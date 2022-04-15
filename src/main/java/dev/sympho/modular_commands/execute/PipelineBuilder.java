package dev.sympho.modular_commands.execute;

import java.util.List;
import java.util.Optional;

import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.sympho.modular_commands.api.command.Command;
import dev.sympho.modular_commands.api.command.Command.Scope;
import dev.sympho.modular_commands.api.command.Invocation;
import dev.sympho.modular_commands.api.command.context.AnyCommandContext;
import dev.sympho.modular_commands.api.command.context.CommandContext;
import dev.sympho.modular_commands.api.command.context.LazyContext;
import dev.sympho.modular_commands.api.command.handler.InvocationHandler;
import dev.sympho.modular_commands.api.command.handler.ResultHandler;
import dev.sympho.modular_commands.api.command.result.CommandContinue;
import dev.sympho.modular_commands.api.command.result.CommandError;
import dev.sympho.modular_commands.api.command.result.CommandErrorException;
import dev.sympho.modular_commands.api.command.result.CommandResult;
import dev.sympho.modular_commands.api.command.result.Results;
import dev.sympho.modular_commands.api.exception.FailureException;
import dev.sympho.modular_commands.api.exception.IncompleteHandlingException;
import dev.sympho.modular_commands.api.registry.Registry;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.Event;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuple4;
import reactor.util.function.Tuples;

/**
 * Type responsible for building a command processing pipeline.
 *
 * @param <E> The type of event that triggers commands.
 * @param <C> The type of commands executed by the pipeline.
 * @param <CTX> The type of command context.
 * @param <IH> The type of invocation handler.
 * @param <RH> The type of result handler.
 * @version 1.0
 * @since 1.0
 * @apiNote Note that, for the purposes of this class, there is a distinction between <i>args</i>
 *          and <i>arguments</i>:
 * 
 *          <ul>
 *              <li><i>args</i> is the raw sequence of strings provided by the triggering event
 *                  that identifies the command to execute. It may or may not also contain, after
 *                  the command identifiers, strings that should be parsed into command 
 *                  <i>arguments</i>.</li>
 *              <li><i>arguments</i> are the values that satisfy the defined parameters of a
 *                  command. They may be obtain from <i>args</i> that appear after the command
 *                  identification, or directly from the event in some way (or both).</li>
 *          </ul>
 */
public abstract class PipelineBuilder<E extends Event, C extends Command, 
        CTX extends CommandContext & LazyContext, 
        IH extends InvocationHandler, RH extends ResultHandler> {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger( PipelineBuilder.class );

    /* Public interface */

    /**
     * Builds a new pipeline with the given client and registry.
     *
     * @param client The client to receive events from.
     * @param registry The registry to use to look up commands.
     * @return The built processing pipeline.
     */
    public Mono<Void> buildPipeline( final GatewayDiscordClient client, final Registry registry ) {

        final Flux<E> source = client.on( eventType() )
                .filter( this::eventFilter )
                .doOnNext( e -> {
                    LOGGER.trace( "Received event: {}", e );
                } );
        return buildPipeline( source, registry ).retry();

    }

    /**
     * Builds the processing pipeline.
     *
     * @param source The event source.
     * @param registry The registry to use to look up commands.
     * @return The built processing pipeline.
     */
    @SideEffectFree
    private Mono<Void> buildPipeline( final Flux<E> source, final Registry registry ) {

        return source.flatMap( event -> parseEvent( event, registry )
                .flatMap( this::executeCommand )
                .doOnNext( ctx -> {
                    final CTX context = ctx.getT2();
                    final CommandResult result = ctx.getT3();

                    if ( result instanceof CommandErrorException r ) {
                        final var cause = r.cause();
                        LOGGER.error( String.format( "Exception while executing command %s", 
                                context.getInvocation() ), cause );
                    } else if ( result instanceof CommandError r ) {
                        LOGGER.error( "Error while executing command {}: {}", 
                                context.getInvocation(), r.message() );
                    } else {
                        LOGGER.debug( "Finished command execution {} with result {}", 
                                context.getInvocation(), result.getClass().getSimpleName() );
                        LOGGER.trace( "{} => {}", context.getInvocation(), result );
                    }
                } )
                .flatMap( this::handleResult )
                .doOnNext( c -> {
                    LOGGER.warn( "Handling of result of command {} not complete", 
                            c.getInvocation() );
                } )
                .onErrorResume( e -> {
                    LOGGER.error( "Exception thrown within processing pipeline", e );
                    return Mono.empty();
                } )
        ).then();

    }

    /* Subclass methods */

    /**
     * Retrieves the event type to listen for.
     *
     * @return The event type.
     */
    @Pure
    protected abstract Class<E> eventType();

    /**
     * Retrives the command type to use.
     *
     * @return The command type.
     */
    @Pure
    protected abstract Class<C> commandType();

    // BEGIN BUGGED PARAGRAPH
    /**
     * Determines whether a parsed arg list (as given by {@link #parse(Event)}) must always
     * be fully matched to a command. If {@code true}, the presence of leftover args (as would
     * be received by {@link #makeContext(Event, Command, Invocation, List)}) triggers an error
     * for that event (stopping the processing of that particular event, but without stopping
     * the pipeline).
     *
     * @return {@code true} if the parsed args must be fully matched to a command.
     *         {@code false} if additional args are allowed.
     * @apiNote This check is not related to any user-facing functionality, but purely as an
     *          internal sanity check. If an event provides arguments in a way other than the
     *          parsed args, with said args being used only to identify the command to execute,
     *          it would not make sense for extra args to be received.
     * 
     *          <p>For example, in a slash command, the comand name is provided separately from
     *          the arguments, and any subcommands are already labeled as such, so there is no
     *          need to evaluate any further args when looking up the command. At the same
     *          time, since slash commands (including subcommands) have to be registered with 
     *          Discord ahead of time to be callable by users, the only way that a slash command 
     *          event would provide a subcommand that is not found is if the command was registered
     *          to Discord without having a corresponding internal handler, without would indicate
     *          an inconsistency in the command system (the <i>declared</i> commands do not match
     *          the <i>implemented</i> commands), which should be reported to the administrator
     *          as a (non-fatal) pipeline error.
     */
    // END BUGGED PARAGRAPH
    @Pure
    protected abstract boolean fullMatch();

    /**
     * Determines if an event should be processed for commands. This filter is applied at the
     * start of handling, before any parsing is performed.
     *
     * @param event The event to check.
     * @return {@code true} if the event should be processed.
     *         {@code false} if the event should be discarded.
     * @implSpec The default is to process all received events.
     */
    @Pure
    protected boolean eventFilter( final E event ) {
        return true;
    }

    /**
     * Retrieves the validator to use for validating command invocations.
     * 
     * <p>For performance reasons, the validator should be implemented statelessly, with a single
     * instance being created by the implenting builder which is then returned every time. This
     * avoids the cost of constructing a new instance every time.
     * 
     * <p>Note that there is no reason why a validator implementation would need to keep
     * state, as it is intended to be a simple filter.
     *
     * @return The validator to use.
     */
    @Pure
    protected abstract InvocationValidator<E> getValidator();

    /**
     * Parses the raw args from the event. This includes the names that identify the command
     * and subcommands, as well as any additional args, {@link #fullMatch() if allowed}.
     *
     * @param event The event to parse args from.
     * @return The parsed args. The list may be empty if the message should not be handled
     *         as a command.
     */
    @SideEffectFree
    protected abstract List<String> parse( E event );

    /**
     * Creates a command context from a parsed invocation.
     *
     * @param event The event being processed.
     * @param command The identified command.
     * @param invocation The invocation that mapped to the command. This may be different
     *                   from the command's declared {@link Command#invocation()} if it
     *                   was invoked using an alias (when supported).
     * @param args The args that remained after removing the command identifiers. If
     *             {@link #fullMatch()} is {@code true}, this will always be empty.
     * @return The context that represents the given invocation.
     */
    @SideEffectFree
    protected abstract CTX makeContext( E event, C command, Invocation invocation, 
            List<String> args );

    /**
     * Retrieves the ID of the guild where the command was invoked, if any, from the
     * triggering event.
     *
     * @param event The triggering event.
     * @return The ID of the guild where the event was invoked.
     */
    @SideEffectFree
    protected abstract Optional<Snowflake> getGuildId( E event );

    /**
     * Retreives the invocation handler specified by the given command.
     *
     * @param command The command.
     * @return The invocation handler.
     */
    @Pure
    protected abstract IH getInvocationHandler( C command );

    /**
     * Invokes the given handler with the given context.
     *
     * @param handler The handler to use.
     * @param context The context to use.
     * @return The result of the invocation.
     * @throws Exception if an error occurred.
     * @see InvocationHandler#handle(AnyCommandContext)
     */
    protected abstract Mono<CommandResult> invoke( IH handler, CTX context ) throws Exception;

    /**
     * Retreives the result handlers specified by the given command.
     *
     * @param command The command.
     * @return The result handlers.
     */
    @Pure
    protected abstract List<? extends RH> getResultHandlers( C command );

    /**
     * Invokes the given handler with the given context and result.
     *
     * @param handler The handler to use.
     * @param context The context to use.
     * @param result The result to use.
     * @return The handling result.
     * @see ResultHandler#handle(AnyCommandContext, CommandResult)
     */
    protected abstract Mono<Boolean> handle( RH handler, CTX context, CommandResult result );

    /* Helpers */

    /**
     * Invokes a handler while wrapping any thrown exceptions into a result.
     *
     * @param handler The handler to use.
     * @param context The context to use.
     * @return The result of the invocation.
     */
    @SuppressWarnings( "checkstyle:illegalcatch" )
    private Mono<CommandResult> invokeWrap( final IH handler, final CTX context ) {

        try {
            return invoke( handler, context )
                    .onErrorResume( FailureException.class, e -> Mono.just( e.getResult() ) )
                    .onErrorResume( e -> Results.exceptionMono( e ) );
        } catch ( final FailureException e ) {
            return Mono.just( e.getResult() );
        } catch ( final Exception e ) {
            return Results.exceptionMono( e );
        }

    }

    /**
     * Verifies that the invocation is valid for the scope that the command is defined in.
     *
     * @param payload The invocation payload.
     * @return {@code true} if the invocation is valid and the command should be executed.
     */
    @Pure
    private boolean checkScope( final Tuple4<E, List<C>, Invocation, List<String>> payload ) {

        final E event = payload.getT1();
        final List<C> chain = payload.getT2();

        final C command = InvocationUtils.getInvokedCommand( chain );

        return command.scope() == Scope.GLOBAL || getGuildId( event ).isPresent();

    }

    /**
     * Verifies that the invoked command can be invoked directly.
     *
     * @param payload The invocation payload.
     * @return {@code true} if the invocation is valid and the command should be executed.
     */
    @Pure
    private boolean checkCallable( final Tuple4<E, List<C>, Invocation, List<String>> payload ) {

        final List<C> chain = payload.getT2();

        final C command = InvocationUtils.getInvokedCommand( chain );

        return command.callable();

    }

    /**
     * Parses an invocation from an event.
     * 
     * <p>Invocations of a command in an incompatible scope or of a command that cannot
     * be invoked by itself are pre-filtered and will return an empty Mono.
     *
     * @param event The event.
     * @param registry The registry to use for command lookups.
     * @return A Mono that issues the parsed command, if any. 
     */
    @SideEffectFree
    private Mono<Tuple4<E, List<C>, Invocation, List<String>>> parseEvent( 
            final E event, final Registry registry ) {

        return Mono.just( event )
                .map( e -> Tuples.of( e, parse( e ) ) )
                .filter( ctx -> !ctx.getT2().isEmpty() )
                .mapNotNull( ctx -> {
                    final E e = ctx.getT1();
                    final List<String> args = ctx.getT2();

                    LOGGER.trace( "Parsed args {}", args );

                    final List<C> chain = InvocationUtils.makeChain( 
                            registry, args, commandType() );

                    final Invocation invocation = new Invocation( args.subList( 0, chain.size() ) );

                    final var remainder = args.subList( chain.size(), args.size() );
                    if ( fullMatch() && !remainder.isEmpty() ) {
                        throw new IllegalStateException( 
                            "No full match found: " + remainder.toString() 
                            + " was leftover from " + args.toString()
                        );
                    }

                    LOGGER.trace( "Matched invocation {}", invocation );

                    return Tuples.of( e, chain, invocation, remainder );
                } )
                .filter( ctx -> !ctx.getT2().isEmpty() )
                .filter( this::checkScope )
                .filter( this::checkCallable )
                .name( "command-parse" )
                .metrics();

    }

    /**
     * Validates that an invocation was appropriate.
     *
     * @param event The event that triggered the invocation.
     * @param chain The command chain.
     * @return A Mono that completes successfully if the invocation is appropriate,
     *         otherwise issuing a {@link ResultException} error with an error result.
     */
    @SideEffectFree
    private Mono<Void> validateCommand( final E event, final List<? extends Command> chain ) {

        final var validator = getValidator();

        return validator.validateSettings( event, chain )
                .thenEmpty( validator.validateDiscordPermissions( event, chain ) )
                .name( "command-validate" ).metrics();

    }

    /**
     * Verifies that the invocation of a command was completely handled. That is,
     * that the final result was not {@link CommandContinue continue}.
     *
     * @param result The final invocation result.
     * @param chain The command chain.
     * @param context The invocation context.
     * @return {@code true} if execution was fully handled.
     * @throws IncompleteHandlingException if the execution was not fully handled.
     */
    private boolean verifyHandled( final CommandResult result, final List<C> chain, 
            final CTX context ) throws IncompleteHandlingException {

        if ( result instanceof CommandContinue ) {
            throw new IncompleteHandlingException( chain, context.getInvocation() );
        } else {
            return true;
        }

    }

    /**
     * Invokes a command chain.
     *
     * @param chain The command chain.
     * @param context The invocation context.
     * @return A Mono that issues the final result once invocation is complete.
     */
    private Mono<CommandResult> invokeCommand( final List<C> chain, final CTX context ) {

        final var invocation = InvocationUtils.getInvokedCommand( chain ).invocation();
        LOGGER.debug( "Invoking command {}", invocation );

        final List<IH> handlers = InvocationUtils.accumulateHandlers(
                chain, this::getInvocationHandler );
        LOGGER.trace( "Handlers for {}: {}", invocation, handlers );

        var state = Results.contMono();
        for ( final var handler : handlers ) {
            state = state.flatMap( r -> {
                if ( r instanceof CommandContinue ) {
                    LOGGER.trace( "Invoking command handler for {}", invocation );
                    return invokeWrap( handler, context );
                } else {
                    LOGGER.trace( "Skipping command handler for {}", invocation );
                    return Mono.just( r );
                }
            } );
        }

        return state.filter( r -> verifyHandled( r, chain, context ) )
                .name( "command-invoke" ).metrics();

    }

    /**
     * Executes a command under an invocation.
     *
     * @param payload The invocation payload.
     * @return A Mono that issues the invocation result once execution has completed.
     */
    private Mono<Tuple3<C, CTX, CommandResult>> executeCommand( 
            final Tuple4<E, List<C>, Invocation, List<String>> payload ) {

        final E event = payload.getT1();
        final List<C> chain = payload.getT2();
        final Invocation invocation = payload.getT3();
        final List<String> args = payload.getT4();

        final C command = InvocationUtils.getInvokedCommand( chain );
        final CTX context = makeContext( event, command, invocation, args );

        return validateCommand( event, chain )
                .thenReturn( context )
                .flatMap( ctx -> ctx.load().thenReturn( ctx ) )
                .flatMap( ctx -> invokeCommand( chain, ctx ) )
                .onErrorResume( ResultException.class, e -> Mono.just( e.getResult() ) )
                .map( result -> Tuples.of( command, context, result ) )
                .name( "command-execute" ).metrics();

    }

    /**
     * Handles the result of an invocation.
     *
     * @param payload The invocation result.
     * @return A Mono that completes without issuing any value if the result was fully
     *         handled. If the result handling was not complete for any reason, it
     *         issues the invocation context.
     */
    private Mono<CTX> handleResult( final Tuple3<C, CTX, CommandResult> payload ) {

        final C command = payload.getT1();
        final CTX context = payload.getT2();
        final CommandResult result = payload.getT3();

        var state = Mono.just( context );
        for ( final RH handler : getResultHandlers( command ) ) {
            state = state.filterWhen( c -> handle( handler, c, result ) );
        }

        return state.name( "command-result" ).metrics();

    }
    
}
