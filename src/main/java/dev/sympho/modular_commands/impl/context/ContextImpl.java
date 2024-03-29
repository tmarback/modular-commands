package dev.sympho.modular_commands.impl.context;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import org.checkerframework.checker.interning.qual.FindDistinct;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.sympho.bot_utils.access.AccessManager;
import dev.sympho.bot_utils.event.AbstractRepliableContext;
import dev.sympho.bot_utils.event.reply.ReplyManager;
import dev.sympho.modular_commands.api.command.Command;
import dev.sympho.modular_commands.api.command.Invocation;
import dev.sympho.modular_commands.api.command.parameter.Parameter;
import dev.sympho.modular_commands.api.command.parameter.parse.ArgumentParser;
import dev.sympho.modular_commands.api.command.parameter.parse.AttachmentParser;
import dev.sympho.modular_commands.api.command.parameter.parse.BooleanParser;
import dev.sympho.modular_commands.api.command.parameter.parse.ChannelArgumentParser;
import dev.sympho.modular_commands.api.command.parameter.parse.FloatParser;
import dev.sympho.modular_commands.api.command.parameter.parse.IntegerParser;
import dev.sympho.modular_commands.api.command.parameter.parse.InvalidArgumentException;
import dev.sympho.modular_commands.api.command.parameter.parse.MessageArgumentParser;
import dev.sympho.modular_commands.api.command.parameter.parse.RoleArgumentParser;
import dev.sympho.modular_commands.api.command.parameter.parse.SnowflakeParser;
import dev.sympho.modular_commands.api.command.parameter.parse.StringParser;
import dev.sympho.modular_commands.api.command.parameter.parse.UserArgumentParser;
import dev.sympho.modular_commands.api.command.result.CommandFailureArgumentInvalid;
import dev.sympho.modular_commands.api.command.result.CommandFailureArgumentMissing;
import dev.sympho.modular_commands.api.command.result.CommandResult;
import dev.sympho.modular_commands.api.command.result.Results;
import dev.sympho.modular_commands.api.exception.ResultException;
import dev.sympho.modular_commands.execute.InstrumentedContext;
import dev.sympho.modular_commands.execute.LazyContext;
import dev.sympho.modular_commands.execute.Metrics;
import dev.sympho.modular_commands.utils.parse.ParseUtils;
import dev.sympho.reactor_utils.concurrent.ReactiveLatch;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.Event;
import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Channel;
import io.micrometer.observation.ObservationRegistry;
import reactor.core.observability.micrometer.Micrometer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Base implementation for context objects.
 *
 * @param <A> The type that raw arguments are received in.
 * @param <E> The event type.
 * @version 1.0
 * @since 1.0
 */
@SuppressWarnings( "MultipleStringLiterals" )
abstract class ContextImpl<A extends @NonNull Object, E extends @NonNull Event> 
        extends AbstractRepliableContext<E>
        implements LazyContext, InstrumentedContext {

    /** The prefix for metrics in this class. */
    public static final String METRIC_NAME_PREFIX = "context";
    /** The prefix for argument parsing metrics. */
    public static final String METRIC_NAME_PREFIX_ARGUMENT = "argument";

    /** The logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger( ContextImpl.class );

    /** The metric prefix for initialization. */
    private static final String METRIC_NAME_INITIALIZE = Metrics.name( METRIC_NAME_PREFIX, "init" );
    /** The metric prefix for loading. */
    private static final String METRIC_NAME_LOAD = Metrics.name( METRIC_NAME_PREFIX, "load" );
    /** The metric prefix for argument initialization. */
    private static final String METRIC_NAME_ARGUMENT_INIT = Metrics.name( METRIC_NAME_PREFIX, 
            METRIC_NAME_PREFIX_ARGUMENT, "init" );
    /** The metric prefix for parsing all arguments. */
    private static final String METRIC_NAME_ARGUMENT_PARSE_ALL = Metrics.name( METRIC_NAME_PREFIX, 
            METRIC_NAME_PREFIX_ARGUMENT, "all" );
    /** The metric prefix for parsing one argument. */
    private static final String METRIC_NAME_ARGUMENT_PARSE_ONE = Metrics.name( METRIC_NAME_PREFIX, 
            METRIC_NAME_PREFIX_ARGUMENT, "one" );

    /** The tag name for the parameter name. */
    private static final String METRIC_TAG_PARAMETER = Metrics.name( "parameter" );

    /** The invoked command. */
    protected final Command<?> command;

    /** The invocation that triggered this context. */
    private final Invocation invocation;

    /** Storage for context objects. */
    private final Map<String, @Nullable Object> context;

    /** The parsed arguments. */
    private @MonotonicNonNull Map<String, ? extends Argument<?>> arguments;

    /** Marks if loaded or not. */
    private final AtomicBoolean initialized;

    /** Latch that marks if loading finished. */
    private final ReactiveLatch initializeLatch;

    /** The result of {@link #load()}. */
    private @MonotonicNonNull Mono<CommandResult> loadResult;

    /**
     * Initializes a new context.
     *
     * @param event The event that triggered the invocation.
     * @param invocation The invocation that triggered execution.
     * @param command The invoked command.
     * @param accessManager The access manager to use.
     * @param replyManager The reply manager to use.
     */
    protected ContextImpl( 
            final E event,
            final Invocation invocation, final Command<?> command, 
            final AccessManager accessManager, final ReplyManager replyManager
    ) {

        super( event, accessManager, replyManager );

        this.command = command;
        this.invocation = invocation;

        this.context = new HashMap<>();

        this.arguments = null;

        this.initialized = new AtomicBoolean( false );
        this.initializeLatch = new ReactiveLatch();
        this.loadResult = null;

    }

    /* Argument getters */

    /**
     * Retrieves the string argument associated with the parameter of the given name.
     *
     * @param name The parameter name.
     * @return The associated string argument. May be empty if missing.
     * @throws InvalidArgumentException if the received argument is not a valid string.
     */
    @SideEffectFree
    protected abstract Mono<String> getStringArgument( String name ) 
            throws InvalidArgumentException;

    /**
     * Retrieves the boolean argument associated with the parameter of the given name.
     *
     * @param name The parameter name.
     * @return The associated boolean argument. May be empty if missing or fail with
     *         a {@link InvalidArgumentException} if the value received is invalid.
     * @throws InvalidArgumentException if the received argument is not a valid integer.
     */
    @SideEffectFree
    protected abstract Mono<Boolean> getBooleanArgument( String name ) 
            throws InvalidArgumentException;

    /**
     * Retrieves the integer argument associated with the parameter of the given name.
     *
     * @param name The parameter name.
     * @return The associated integer argument. May be empty if missing or fail with
     *         a {@link InvalidArgumentException} if the value received is invalid.
     * @throws InvalidArgumentException if the received argument is not a valid integer.
     */
    @SideEffectFree
    protected abstract Mono<Long> getIntegerArgument( String name ) 
            throws InvalidArgumentException;

    /**
     * Retrieves the float argument associated with the parameter of the given name.
     *
     * @param name The parameter name.
     * @return The associated float argument. May be empty if missing or fail with
     *         a {@link InvalidArgumentException} if the value received is invalid.
     * @throws InvalidArgumentException if the received argument is not a valid float.
     */
    @SideEffectFree
    protected abstract Mono<Double> getFloatArgument( String name ) 
            throws InvalidArgumentException;

    /**
     * Retrieves the snowflake argument associated with the parameter of the given name.
     *
     * @param name The parameter name.
     * @param type The ID type.
     * @return The associated snowflake argument. May be empty if missing or fail with
     *         a {@link InvalidArgumentException} if the value received is invalid.
     * @throws InvalidArgumentException if the received argument is not a valid snowflake.
     */
    @SideEffectFree
    protected abstract Mono<Snowflake> getSnowflakeArgument( String name, 
            SnowflakeParser.Type type ) throws InvalidArgumentException;

    /**
     * Retrieves the user argument associated with the parameter of the given name.
     *
     * @param name The parameter name.
     * @return The associated user argument. May be empty if missing or fail with
     *         a {@link InvalidArgumentException} if the value received is invalid.
     */
    @SideEffectFree
    protected abstract Mono<User> getUserArgument( String name );

    /**
     * Retrieves the role argument associated with the parameter of the given name.
     *
     * @param name The parameter name.
     * @return The associated role argument. May be empty if missing or fail with
     *         a {@link InvalidArgumentException} if the value received is invalid.
     */
    @SideEffectFree
    protected abstract Mono<Role> getRoleArgument( String name );

    /**
     * Retrieves the channel argument associated with the parameter of the given name.
     *
     * @param <C> The channel type.
     * @param name The parameter name.
     * @param type The channel type.
     * @return The associated channel argument. May be empty if missing or fail with
     *         a {@link InvalidArgumentException} if the value received is invalid.
     */
    @SideEffectFree
    protected abstract <C extends @NonNull Channel> Mono<C> getChannelArgument( String name, 
            Class<C> type );

    /**
     * Retrieves the message argument associated with the parameter of the given name.
     *
     * @param name The parameter name.
     * @return The associated message argument. May be empty if missing or fail with
     *         a {@link InvalidArgumentException} if the value received is invalid.
     * @implSpec By default, parses a string argument of the same name.
     */
    @SideEffectFree
    protected Mono<Message> getMessageArgument( final String name ) {

        return getStringArgument( name )
                .flatMap( raw -> ParseUtils.MESSAGE.parse( this, raw ) );

    }

    /**
     * Retrieves the attachment argument associated with the parameter of the given name.
     *
     * @param name The parameter name.
     * @return The associated attachment argument. May be empty if missing.
     */
    @SideEffectFree
    protected abstract Mono<Attachment> getAttachmentArgument( String name );

    /* Argument parsing */

    /**
     * Handles a missing argument according to the parameter specification.
     *
     * @param <R> The raw argument type.
     * @param parameter The parameter whose argument is missing.
     * @return A Mono that fails with a {@link ResultException} if the parameter is not
     *         allowed to be missing, and is empty otherwise.
     * @implNote The error case uses lazy instantiation. Use without 
     *           {@link Mono#defer(java.util.function.Supplier)}.
     */
    @SideEffectFree
    private <R extends @NonNull Object> Mono<R> handleMissingArgument( 
            final Parameter<?> parameter ) {

        if ( parameter.required() ) {
            return Mono.error( () -> new ResultException( 
                    new CommandFailureArgumentMissing( parameter ) 
            ) );
        } else {
            return Mono.empty();
        }

    }

    /**
     * Wraps the error for an invalid parameter into a result.
     *
     * @param parameter The parameter.
     * @param exception The exeception that was caused.
     * @return The wrapped result.
     */
    @SideEffectFree
    private ResultException wrapInvalidParam( final Parameter<?> parameter,
            final InvalidArgumentException exception ) {

        LOGGER.trace( "Invalid argument for parameter {}: {}", parameter, exception.getMessage() );
        final var error = exception.getMessage();
        final var result = new CommandFailureArgumentInvalid( parameter, error );
        return new ResultException( result );

    }

    /**
     * Wraps an error encountered during parameter parsing into an error result.
     *
     * @param parameter The parameter.
     * @param error The error.
     * @return The wrapped result.
     */
    @SideEffectFree
    private ResultException wrapParamError( final Parameter<?> parameter, final Throwable error ) {

        // Sanity check
        if ( error instanceof ResultException res ) {
            LOGGER.warn( "Result exception would be wrapped" );
            return res;
        }

        LOGGER.error( "Error while parsing parameter {}: {}", parameter, error.getMessage() );
        return new ResultException( Results.exceptionR( error ) );

    }

    /**
     * Parses an argument.
     *
     * @param <R> The raw argument type.
     * @param <T> The parsed argument type.
     * @param parameter The parameter specification.
     * @param getter The function to use to get the raw argument.
     * @param parser The parser to use.
     * @return A Mono that issues the parsed argument. May fail with a 
     *         {@link ResultException} if the raw value is invalid or missing 
     *         (and required) or if an error happened, and may be empty if no value.
     */
    @SideEffectFree
    @SuppressWarnings( { "conditional", "return" } ) // Weird Optional stuff
    private <R extends @NonNull Object, T extends @NonNull Object> Mono<T> parseArgument(
            final Parameter<T> parameter,
            final Function<String, Mono<R>> getter,
            final ArgumentParser<R, T> parser 
    ) {

        return getter.apply( parameter.name() )
                .switchIfEmpty( handleMissingArgument( parameter ) )
                .map( parser::validateRaw )
                // Note flatMap automatically packs exceptions thrown by parse()
                .flatMap( raw -> parser.parse( this, raw ) )
                .switchIfEmpty( Mono.justOrEmpty( parameter.defaultValue() ) )
                .onErrorMap( InvalidArgumentException.class, 
                        e -> wrapInvalidParam( parameter, e ) 
                )
                .onErrorMap( e -> !( e instanceof ResultException ), 
                        e -> wrapParamError( parameter, e ) 
                );

    }

    /**
     * Parses a channel argument.
     *
     * @param <C> The channel type.
     * @param <T> The parsed argument type.
     * @param parameter The parameter specification.
     * @param parser The parser to use.
     * @return A Mono that issues the parsed argument. May fail with a 
     *         {@link ResultException} if the raw value is invalid or missing 
     *         (and required) or if an error happened, and may be empty if no value.
     * @apiNote Channel gets an additional wrapper in order to carry the C generic.
     */
    private <C extends @NonNull Channel, T extends @NonNull Object> Mono<T> parseArgument(
            final Parameter<T> parameter,
            final ChannelArgumentParser<C, T> parser
    ) {

        return parseArgument(
                parameter, 
                name -> getChannelArgument( name, parser.type() ), 
                parser 
        );

    }

    /**
     * Parses an argument.
     *
     * @param <T> The parsed argument type.
     * @param parameter The parameter to parse.
     * @return A Mono that issues the parsed argument. May fail with a 
     *         {@link ResultException} if the raw value is invalid or missing 
     *         (and required) or if an error happened, and may be empty if no value.
     */
    @SideEffectFree
    @SuppressWarnings( { "JavadocMethod", "unchecked" } ) // Ignore undeclared exception
    private <T extends @NonNull Object> Mono<T> parseArgument( final Parameter<T> parameter ) {

        // Note: Cannot use <T> directly in the instanceof because Generics
        // is horribly limited and can't even realize that an ArgumentParser<?, T> that is an 
        // instance of AttachmentParser is, by definition, an instance of AttachmentParser<T> 
        // (same for other types).
        final var parser = parameter.parser();
        if ( parser instanceof AttachmentParser<?> p ) {
            return parseArgument( parameter, this::getAttachmentArgument, 
                    ( AttachmentParser<T> ) p );
        } else if ( parser instanceof StringParser<?> p ) {
            return parseArgument( parameter, this::getStringArgument, ( StringParser<T> ) p );
        } else if ( parser instanceof BooleanParser<?> p ) {
            return parseArgument( parameter, this::getBooleanArgument, ( BooleanParser<T> ) p );
        } else if ( parser instanceof IntegerParser<?> p ) {
            return parseArgument( parameter, this::getIntegerArgument, ( IntegerParser<T> ) p );
        } else if ( parser instanceof FloatParser<?> p ) {
            return parseArgument( parameter, this::getFloatArgument, ( FloatParser<T> ) p );
        } else if ( parser instanceof SnowflakeParser<?> p ) {
            return parseArgument( parameter, n -> getSnowflakeArgument( n, p.type() ), 
                    ( SnowflakeParser<T> ) p );
        } else if ( parser instanceof UserArgumentParser<?> p ) {
            return parseArgument( parameter, this::getUserArgument, 
                    ( UserArgumentParser<T> ) p );
        } else if ( parser instanceof RoleArgumentParser<?> p ) {
            return parseArgument( parameter, this::getRoleArgument, 
                    ( RoleArgumentParser<T> ) p );
        } else if ( parser instanceof ChannelArgumentParser<?, ?> p ) {
            return parseArgument( parameter, ( ChannelArgumentParser<?, T> ) p );
        } else if ( parser instanceof MessageArgumentParser<?> p ) {
            return parseArgument( parameter, this::getMessageArgument, 
                    ( MessageArgumentParser<T> ) p );
        } else { // Should never happen
            throw new IllegalArgumentException( "Unrecognized parser type: " + parser.getClass() );
        }

    }

    /**
     * Processes an argument.
     *
     * @param <T> The parsed argument type.
     * @param parameter The parameter specification.
     * @return A Mono that issues the parameter name and the processed argument. May fail 
     *         with a {@link ResultException} if the raw value is invalid or is missing 
     *         (and is required) or if an error happened.
     */
    @SideEffectFree
    @SuppressWarnings( { 
            "return", // Bug with type inference in checker
            "optional.parameter" // TODO: Weird interaction with generics?
    } ) 
    private <T extends @NonNull Object> Mono<? extends Entry<String, ? extends Argument<T>>>
            processArgument( final Parameter<T> parameter ) {

        return parseArgument( parameter )
                .singleOptional()
                .map( v -> new Argument<>( parameter, v.orElse( null ) ) )
                .map( a -> Map.entry( parameter.name(), a ) )
                .doOnError( t -> {
                    if ( t instanceof ResultException ex ) {
                        LOGGER.trace( "Arg processing aborted: {}", ex.getResult() );
                    } else {
                        LOGGER.error( "Failed to process argument", t );
                    }
                } );

    }

    /* Initialization */

    /**
     * Performs any required initialization of received arguments before
     * parsing starts.
     *
     * @return A mono that completes when initialization is done.
     */
    protected abstract Mono<Void> initArgs();

    /* Implementations */

    @Override
    public String getCommandId() {

        return command.id();

    }

    @Override
    public Invocation invocation() {

        return invocation;

    }

    @Override
    public Invocation commandInvocation() {

        return command.invocation();

    }

    /**
     * Retrieves the argument with the given name.
     *
     * @param name The parameter name.
     * @return The argument.
     * @throws IllegalStateException if the context is not yet loaded.
     * @throws IllegalArgumentException if there is no parameter with the given name.
     */
    @Pure
    private Argument<?> getArgument( final String name ) 
            throws IllegalStateException, IllegalArgumentException {

        if ( arguments == null ) {
            throw new IllegalStateException( "Context not loaded yet" );
        }

        final var arg = arguments.get( name );
        if ( arg == null ) {
            throw new IllegalArgumentException( String.format( "No parameter named '%s'", name ) );
        } else {
            return arg;
        }

    }

    @Override
    public <T extends @NonNull Object> @Nullable T getArgument( 
            final String name, final Class<T> argumentType )
            throws IllegalArgumentException, ClassCastException {

        return getArgument( name ).getValue( argumentType );

    }

    @Override
    public <T extends @NonNull Object> @Nullable T getArgument( 
            final @FindDistinct Parameter<? extends T> parameter ) throws IllegalArgumentException {

        final var argument = getArgument( parameter.name() );
        if ( argument.parameter() == parameter ) {
            // Type is determined by the original parameter, so if the given parameter is the
            // same as the original parameter, the type trivially is the same
            @SuppressWarnings( "unchecked" )
            final Argument<T> arg = ( Argument<T> ) argument;
            return arg.value();
        } else {
            throw new IllegalArgumentException( 
                    "Parameter does not match definition: " + parameter );
        }

    }

    @Override
    public boolean setContext( final String key, final @Nullable Object obj, 
            final boolean replace ) {

        if ( !replace && context.containsKey( key ) ) {
            return false;
        } else {
            context.put( key, obj );
            return true;
        }

    }

    @Override
    @SuppressWarnings( "signedness:return" )
    public <T> @Nullable T getContext( final String key, final Class<? extends T> type )
            throws IllegalArgumentException, ClassCastException {

        if ( !context.containsKey( key ) ) {
            throw new IllegalArgumentException( String.format( 
                    "No context under key '%s'.", key ) );
        }

        return type.cast( context.get( key ) );

    }

    /**
     * Performs the initialize operation.
     *
     * @param observations The observation registry to use.
     */
    private void doInitialize( final ObservationRegistry observations ) {

        LOGGER.trace( "Initializing context" );

        // Prepare load
        // cache() for idempotence and to prevent cancelling
        this.loadResult = Mono.just( observations ).flatMap( this::doLoad ).cache();

        LOGGER.trace( "Context initialized" );

    }

    @Override
    public Mono<Void> initialize( final ObservationRegistry observations ) {

        if ( initialized.getAndSet( true ) ) {
            return initializeLatch.await(); // Already initializing
        }

        LOGGER.trace( "Initializing context" );

        // Prepare load
        // cache() for idempotence and to prevent cancelling
        this.loadResult = Mono.just( observations ).flatMap( this::doLoad ).cache();

        return Mono.fromRunnable( () -> doInitialize( observations ) )
                .then()
                .doOnSuccess( v -> initializeLatch.countDown() )
                .doOnError( initializeLatch::fail )
                .doOnError( t -> LOGGER.error( "Failed to initialize", t ) )
                .checkpoint( METRIC_NAME_INITIALIZE )
                .name( METRIC_NAME_INITIALIZE )
                .transform( this::addTags )
                .tap( Micrometer.observation( observations ) )
                .cache(); // Prevent cancelling

    }

    /**
     * Performs the {@link #load()} operation.
     *
     * @param observations The registry to use for observations.
     * @return The result.
     * @see #load()
     */
    @SuppressWarnings( "assignment" ) // Not detecting null type bounds for some reason
    public Mono<CommandResult> doLoad( final ObservationRegistry observations ) {

        LOGGER.trace( "Loading context" );

        final var init = Mono.defer( () -> initArgs() 
                .checkpoint( METRIC_NAME_ARGUMENT_INIT )
                .name( METRIC_NAME_ARGUMENT_INIT )
                .transform( this::addTags )
                .tap( Micrometer.observation( observations ) )
        );

        final var parse = Mono.defer( () -> Flux.fromIterable( command.parameters() )
                .flatMap( p -> processArgument( p )
                        .checkpoint( METRIC_NAME_ARGUMENT_PARSE_ONE )
                        .name( METRIC_NAME_ARGUMENT_PARSE_ONE )
                        .transform( this::addTags )
                        .tag( METRIC_TAG_PARAMETER, p.name() )
                        .tap( Micrometer.observation( observations ) )
                )
                .collectMap( Entry::getKey, Entry::getValue )
                .doOnNext( args -> {
                    this.arguments = args;
                } )
                .checkpoint( METRIC_NAME_ARGUMENT_PARSE_ALL )
                .name( METRIC_NAME_ARGUMENT_PARSE_ALL )
                .transform( this::addTags )
                .tap( Micrometer.observation( observations ) )
        );

        return init.then( parse )
                .checkpoint( METRIC_NAME_LOAD )
                .name( METRIC_NAME_LOAD )
                .transform( this::addTags )
                .tap( Micrometer.observation( observations ) )
                .doOnSuccess( v -> LOGGER.trace( "Context loaded" ) )
                .doOnError( t -> {
                    if ( t instanceof ResultException ex ) {
                        LOGGER.trace( "Load aborted: {}", ex.getResult() );
                    } else {
                        LOGGER.error( "Failed to load", t );
                    }
                } )
                .then( Mono.empty().cast( CommandResult.class ) )
                .onErrorResume( ResultException.class, e -> Mono.just( e.getResult() ) );

    }

    @Override
    public Mono<CommandResult> load() {

        if ( loadResult == null ) {
            throw new IllegalStateException( "Called load() before initialize()" );
        } else {
            return loadResult;
        }

    }

    /**
     * An argument that corresponds to one of the command's formal parameters.
     *
     * @param <T> The argument type.
     * @param parameter The original parameter this corresponds to.
     * @param value The argument value.
     * @version 1.0
     * @since 1.0
     */
    private record Argument<T extends @NonNull Object>(
            Parameter<T> parameter,
            @Nullable T value 
    ) {

        /**
         * Retrieves the argument value.
         *
         * @param <E> The value type to receive as.
         * @param argumentType The value type.
         * @return The value.
         * @throws ClassCastException if the value is not compatible with the given type.
         */
        @SuppressWarnings( "signedness:return" )
        public <E> @Nullable E getValue( final Class<E> argumentType ) throws ClassCastException {

            return argumentType.cast( value );

        }

    }
    
}
