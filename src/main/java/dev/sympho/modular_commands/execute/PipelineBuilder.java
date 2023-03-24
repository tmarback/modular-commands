package dev.sympho.modular_commands.execute;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.apache.commons.collections4.ListUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.sympho.modular_commands.api.command.Command;
import dev.sympho.modular_commands.api.command.Command.Scope;
import dev.sympho.modular_commands.api.command.Invocation;
import dev.sympho.modular_commands.api.command.handler.Handlers;
import dev.sympho.modular_commands.api.command.handler.InvocationHandler;
import dev.sympho.modular_commands.api.command.handler.ResultHandler;
import dev.sympho.modular_commands.api.command.result.CommandError;
import dev.sympho.modular_commands.api.command.result.CommandErrorException;
import dev.sympho.modular_commands.api.command.result.CommandFailure;
import dev.sympho.modular_commands.api.command.result.CommandResult;
import dev.sympho.modular_commands.api.command.result.CommandSuccess;
import dev.sympho.modular_commands.api.exception.IncompleteHandlingException;
import dev.sympho.modular_commands.api.permission.AccessValidator;
import dev.sympho.modular_commands.api.registry.Registry;
import dev.sympho.modular_commands.utils.SmartIterator;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.Event;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.observation.ObservationRegistry;
import reactor.core.observability.micrometer.Micrometer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuple4;
import reactor.util.function.Tuples;
import reactor.util.retry.Retry;

/**
 * Type responsible for building a command processing pipeline.
 *
 * @param <E> The type of event that triggers commands.
 * @param <CTX> The type of command context.
 * @param <H> The type of handlers.
 * @param <I> The iterator type used to traverse the received arguments.
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
public abstract class PipelineBuilder<E extends Event, 
        CTX extends InstrumentedContext & LazyContext, H extends Handlers, 
        I extends SmartIterator<String>> {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger( PipelineBuilder.class );

    /** The metric prefix for the overall pipeline. */
    private static final String METRIC_NAME_PIPELINE = Metrics.name( "pipeline" );
    /** The metric prefix for event handling. */
    private static final String METRIC_NAME_EVENT = Metrics.name( "event" );
    /** The metric prefix for event parsing. */
    private static final String METRIC_NAME_PARSE = Metrics.name( "parse" );
    /** The metric prefix for command validation. */
    private static final String METRIC_NAME_VALIDATE = Metrics.name( "validate" );
    /** The metric prefix for command invocation. */
    private static final String METRIC_NAME_INVOKE = Metrics.name( "invoke" );
    /** The metric prefix for handler invocation. */
    private static final String METRIC_NAME_HANDLE = Metrics.name( "handle" );
    /** The metric prefix for command execution. */
    private static final String METRIC_NAME_EXECUTE = Metrics.name( "execute" );
    /** The metric name for command result handling. */
    private static final String METRIC_NAME_RESULT = Metrics.name( "result" );

    /** The tag name for the command result. */
    private static final String METRIC_TAG_RESULT = Metrics.name( "outcome" );
    /** The tag name for the handler being invoked. */
    private static final String METRIC_TAG_HANDLER = Metrics.name( "handler" );

    /** The maximum number of times to retry on error before giving up. */
    private static final int MAX_RETRIES = 100;
    /** The minimum backoff period after an error. */
    private static final Duration MIN_BACKOFF = Duration.ofSeconds( 1 );
    /** The maximum backoff period after an error. */
    private static final Duration MAX_BACKOFF = Duration.ofHours( 1 );

    /** The access manager to use for access checks. */
    protected final AccessManager accessManager;

    /** The meter registry to use. */
    protected final MeterRegistry meters;

    /** The observation registry to use. */
    protected final ObservationRegistry observations;

    /** 
     * Creates a new instance. 
     *
     * @param accessManager The access manager to use for access checks.
     * @param meters The meter registry to use.
     * @param observations The observation registry to use.
     */
    protected PipelineBuilder( final AccessManager accessManager, 
            final MeterRegistry meters, final ObservationRegistry observations ) {

        this.accessManager = accessManager;
        this.meters = meters;
        this.observations = observations;

    }

    /* Tag value helpers */

    /**
     * Determines the value for the {@link #METRIC_TAG_RESULT result tag}.
     *
     * @param result The execution result.
     * @return The tag value.
     */
    private static String tagResult( final CommandResult result ) {

        if ( result instanceof CommandSuccess ) {
            return "success";
        } else if ( result instanceof CommandFailure ) {
            return "failure";
        } else if ( result instanceof CommandError ) {
            return "error";
        } else {
            return "unknown";
        }

    }

    /**
     * Adds the {@link #METRIC_TAG_RESULT result tag} to the current observation, if any.
     *
     * @param result The execution result.
     */
    private void addTagResult( final CommandResult result ) {

        final var observation = observations.getCurrentObservation();
        if ( observation != null ) {
            observation.lowCardinalityKeyValue( METRIC_TAG_RESULT, tagResult( result ) );
        }

    }

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

                        addTagResult( result );
                    } )
                    .flatMap( this::handleResult )
                    .doOnError( e -> LOGGER.error( 
                            "Exception thrown within processing pipeline", e 
                    ) )
                    .contextCapture()
                    .checkpoint( METRIC_NAME_EVENT )
                    .name( METRIC_NAME_EVENT )
                    .transform( addTags( event ) )
                    .tap( Micrometer.observation( observations ) )
                    .onErrorComplete()
                    .thenReturn( true ) // For metric tracking purposes
            )
            .doOnError( e -> LOGGER.error( "Fatal error", e ) )
            .checkpoint( METRIC_NAME_PIPELINE )
            .name( METRIC_NAME_PIPELINE )
            .transform( tagType()::apply )
            .tap( Micrometer.metrics( meters ) )
            .retryWhen( Retry.backoff( MAX_RETRIES, MIN_BACKOFF )
                    .maxBackoff( MAX_BACKOFF )
                    .transientErrors( true )
            )
            .doOnError( e -> LOGGER.error( "Pipeline closed due to too many errors" ) )
            .then();

    }

    /* Subclass methods */

    /**
     * Determines the {@link Metrics.Tag.Type type tag} for this pipeline.
     *
     * @return The tag.
     */
    @Pure
    protected abstract Metrics.Tag.Type tagType();

    /**
     * Creates a function that adds the common instrumentation tags for the given event to Monos.
     *
     * @param <T> The mono value type.
     * @param event The event.
     * @return The function.
     */
    @SideEffectFree
    protected final <T> Function<Mono<T>, Mono<T>> addTags( final E event ) {

        return m -> m
                .transform( tagType()::apply )
                .transform( Metrics.Tag.Guild.from( getGuildId( event ) )::apply )
                .transform( Metrics.Tag.Channel.from( getChannelId( event ) )::apply )
                .transform( Metrics.Tag.Caller.from( getCaller( event ).getId() )::apply );

    }

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
    protected abstract Class<H> commandType();

    // BEGIN BUGGED PARAGRAPH
    /**
     * Determines whether a parsed arg list (as given by {@link #parse(Event)}) must always
     * be fully matched to a command. If {@code true}, the presence of leftover args (as would
     * be received by {@link #makeContext(Event, Command, Invocation, SmartIterator)}) triggers 
     * an error for that event (stopping the processing of that particular event, but without 
     * stopping the pipeline).
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
     * @return The parsed args. May have no elements if the event should not be handled
     *         as a command.
     */
    @SideEffectFree
    protected abstract I parse( E event );

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
    protected abstract CTX makeContext( E event, Command<? extends H> command, 
            Invocation invocation, I args );

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
     * Retrieves the guild where the command was invoked, if any, from the
     * triggering event.
     *
     * @param event The triggering event.
     * @return The guild where the event was invoked.
     */
    @SideEffectFree
    protected abstract Mono<Guild> getGuild( E event );

    /**
     * Retrieves the ID of the channel where the command was invoked from the
     * triggering event.
     *
     * @param event The triggering event.
     * @return The ID of the channel where the event was invoked.
     */
    @SideEffectFree
    protected abstract Snowflake getChannelId( E event );

    /**
     * Retrieves the channel where the command was invoked from the
     * triggering event.
     *
     * @param event The triggering event.
     * @return The channel where the event was invoked.
     */
    @SideEffectFree
    protected abstract Mono<MessageChannel> getChannel( E event );

    /**
     * Retrieves the user that invoked the command from the
     * triggering event.
     *
     * @param event The triggering event.
     * @return The user that invoked the command.
     */
    @SideEffectFree
    protected abstract User getCaller( E event );

    /**
     * Creates an access validator for the context of the given event.
     *
     * @param event The triggering event.
     * @return The access validator.
     */
    @SideEffectFree
    protected AccessValidator accessValidator( final E event ) {

        final var guild = getGuild( event );
        final var channel = getChannel( event );
        final var caller = getCaller( event );

        return accessManager.validator( guild, channel, caller );
        
    }

    /**
     * Retrieves the invocation handler specified by the given hander set.
     *
     * @param handlers The handler set.
     * @return The invocation handler.
     */
    @Pure
    protected abstract InvocationHandler<? super CTX> getInvocationHandler( H handlers );

    /**
     * Retrieves the result handlers specified by the given hander set.
     *
     * @param handlers The handler set.
     * @return The result handlers.
     */
    @Pure
    protected abstract List<? extends ResultHandler<? super CTX>> getResultHandlers( H handlers );

    /* Helpers */

    /**
     * Verifies that the invocation is valid for the scope that the command is defined in.
     *
     * @param payload The invocation payload.
     * @return {@code true} if the invocation is valid and the command should be executed.
     */
    @Pure
    private boolean checkScope( 
            final Tuple4<E, List<Command<? extends H>>, Invocation, I> payload ) {

        final E event = payload.getT1();
        final List<Command<? extends H>> chain = payload.getT2();

        final var command = InvocationUtils.getInvokedCommand( chain );

        return command.scope() == Scope.GLOBAL || getGuildId( event ).isPresent();

    }

    /**
     * Verifies that the invoked command can be invoked directly.
     *
     * @param payload The invocation payload.
     * @return {@code true} if the invocation is valid and the command should be executed.
     */
    @Pure
    private boolean checkCallable( 
            final Tuple4<E, List<Command<? extends H>>, Invocation, I> payload ) {

        final List<Command<? extends H>> chain = payload.getT2();

        final var command = InvocationUtils.getInvokedCommand( chain );

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
    private Mono<Tuple4<E, List<Command<? extends H>>, Invocation, I>> parseEvent( 
            final E event, final Registry registry ) {

        return Mono.just( event )
                .map( e -> Tuples.of( e, parse( e ) ) )
                .filter( ctx -> ctx.getT2().hasNext() )
                .map( ctx -> {
                    final E e = ctx.getT1();
                    final I args = ctx.getT2();

                    final var parsed = InvocationUtils.parseInvocation( 
                            registry, args, commandType() );

                    final Invocation invocation = parsed.getT1();
                    final List<Command<? extends H>> chain = parsed.getT2();

                    if ( fullMatch() && args.hasNext() ) {
                        throw new IllegalStateException( 
                            "No full match found: " + args.toStream().toList().toString()
                            + " was leftover after " + invocation.toString()
                        );
                    }

                    LOGGER.trace( "Matched invocation {}", invocation );

                    return Tuples.of( e, chain, invocation, args );
                } )
                .filter( ctx -> !ctx.getT2().isEmpty() )
                .filter( this::checkScope )
                .filter( this::checkCallable )
                .checkpoint( METRIC_NAME_PARSE )
                .name( METRIC_NAME_PARSE )
                .transform( addTags( event ) )
                .tap( Micrometer.observation( observations ) );

    }

    /**
     * Validates that an invocation was appropriate.
     *
     * @param event The event that triggered the invocation.
     * @param context The invocation context.
     * @param chain The command chain.
     * @return A Mono that completes empty if the invocation is appropriate,
     *         otherwise issuing a failure result.
     */
    @SideEffectFree
    private Mono<CommandResult> validateCommand( final E event, final CTX context,
            final List<? extends Command<? extends H>> chain ) {

        final var validator = getValidator();
        final var access = accessValidator( event );

        return validator.validateSettings( event, chain )
                .switchIfEmpty( validator.validateAccess( access, chain ) )
                .checkpoint( METRIC_NAME_VALIDATE )
                .name( METRIC_NAME_VALIDATE )
                .transform( context::addTags )
                .tap( Micrometer.observation( observations ) );

    }

    /**
     * Invokes a command chain.
     *
     * @param chain The command chain.
     * @param context The invocation context.
     * @return A Mono that issues the final result once invocation is complete.
     */
    private Mono<CommandResult> invokeCommand( 
            final List<? extends @NonNull Command<? extends @NonNull H>> chain,
            final CTX context ) {

        final var invocation = InvocationUtils.getInvokedCommand( chain ).invocation();
        LOGGER.debug( "Invoking command {}", invocation );

        @SuppressWarnings( "type.argument" ) // @UnknownKeyFor bound weirdness
        final var commands = InvocationUtils.handlingOrder( chain );
        if ( LOGGER.isTraceEnabled() ) {
            LOGGER.trace( "Execution order for {}: {}", invocation, commands.stream()
                    .map( Command::id )
                    .toList() 
            );
        }

        return Flux.fromIterable( commands )
                .concatMap( c -> getInvocationHandler( c.handlers() )
                        .handleWrapped( context )
                        .checkpoint( c.id() )
                        .name( METRIC_NAME_HANDLE )
                        .transform( context::addTags )
                        .tag( METRIC_TAG_HANDLER, c.id() )
                        .tap( Micrometer.observation( observations ) )
                )
                .take( 1 ) // Ensures it stops at the first non-empty result
                .switchIfEmpty( Mono.error( 
                        () -> new IncompleteHandlingException( chain, context.getInvocation() ) 
                ) )
                .single()
                .doOnNext( this::addTagResult )
                .checkpoint( METRIC_NAME_INVOKE )
                .name( METRIC_NAME_INVOKE )
                .transform( context::addTags )
                .tap( Micrometer.observation( observations ) );

    }

    /**
     * Executes a command under an invocation.
     *
     * @param <C> The command type.
     * @param payload The invocation payload.
     * @return A Mono that issues the invocation result once execution has completed.
     * @throws IllegalStateException if some mismatch is detected.
     */
    private <C extends Command<? extends H>> Mono<Tuple3<C, CTX, CommandResult>> executeCommand( 
            final Tuple4<E, List<C>, Invocation, I> payload ) {

        final E event = payload.getT1();
        final List<C> chain = payload.getT2();
        final Invocation invocation = payload.getT3();
        final I args = payload.getT4();

        final var command = InvocationUtils.getInvokedCommand( chain );
        final CTX context = makeContext( event, command, invocation, args );

        // Sanity check that the normalized invocation (e.g. the invocation after resolving 
        // aliases) is the same as the canonical invocation
        final var normalizedInvocation = chain.stream().map( Command::name ).toList();
        if ( !normalizedInvocation.equals( command.invocation().chain() ) ) {
            throw new IllegalStateException( String.format(
                    "Normalized invocation is %s, but command %s has invocation %s",
                    Invocation.of( normalizedInvocation ),
                    command.id(),
                    command.invocation()
            ) );
        }

        return context.initialize( observations )
                .then( Mono.defer( () -> validateCommand( event, context, chain ) ) )
                .switchIfEmpty( Mono.defer( () -> context.load() ) )
                .switchIfEmpty( Mono.defer( () -> invokeCommand( chain, context ) ) )
                .doOnNext( this::addTagResult )
                .map( result -> Tuples.of( command, context, result ) )
                .checkpoint( METRIC_NAME_EXECUTE )
                .name( METRIC_NAME_EXECUTE )
                .transform( context::addTags )
                .tap( Micrometer.observation( observations ) );

    }

    /**
     * Handles the result of an invocation.
     *
     * @param payload The invocation result.
     * @return A Mono that completes once handing is complete.
     */
    private Mono<Void> handleResult( 
            final Tuple3<Command<? extends H>, CTX, CommandResult> payload ) {

        final Command<? extends H> command = payload.getT1();
        final CTX context = payload.getT2();
        final CommandResult result = payload.getT3();

        final var handlers = ListUtils.union( 
                getResultHandlers( command.handlers() ),
                List.of( BaseHandler.get() ) 
        );

        return Flux.fromIterable( handlers )
                .concatMap( h -> h.handle( context, result ) )
                .filter( r -> r )
                .take( 1 ) // Stop once the first one signals complete
                .count()
                .filter( c -> c == 0 ) // No handler signaled complete
                .doOnNext( c -> {
                    LOGGER.warn( "Handling of result of command {} not complete", 
                            context.getInvocation() );
                } )
                .then()
                .checkpoint( METRIC_NAME_RESULT )
                .name( METRIC_NAME_RESULT )
                .transform( context::addTags )
                .tag( METRIC_TAG_RESULT, tagResult( result ) )
                .tap( Micrometer.observation( observations ) );

    }
    
}
