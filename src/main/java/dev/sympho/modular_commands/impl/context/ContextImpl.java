package dev.sympho.modular_commands.impl.context;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import org.checkerframework.checker.calledmethods.qual.EnsuresCalledMethods;
import org.checkerframework.checker.interning.qual.FindDistinct;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.sympho.modular_commands.api.command.Invocation;
import dev.sympho.modular_commands.api.command.ReplyManager;
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
import dev.sympho.modular_commands.api.exception.ResultException;
import dev.sympho.modular_commands.api.permission.AccessValidator;
import dev.sympho.modular_commands.api.permission.Group;
import dev.sympho.modular_commands.execute.LazyContext;
import dev.sympho.modular_commands.utils.ReactiveLatch;
import dev.sympho.modular_commands.utils.parse.ParseUtils;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.core.spec.MessageEditSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

/**
 * Base implementation for context objects.
 *
 * @param <A> The type that raw arguments are received in.
 * @version 1.0
 * @since 1.0
 */
abstract class ContextImpl<A extends @NonNull Object> implements LazyContext {

    /** The logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger( ContextImpl.class );

    /** The command parameters in the order that they should be received. */
    protected final List<Parameter<?>> parameters;

    /** The invocation that triggered this context. */
    private final Invocation invocation;

    /** The validator to use for access checks. */
    private final AccessValidator access;

    /** Storage for context objects. */
    private final Map<String, @Nullable Object> context;

    /** The parsed arguments. */
    private @MonotonicNonNull Map<String, ? extends Argument<?>> arguments;

    /** The reply manager. */
    private @MonotonicNonNull ReplyManager reply;

    /** Marks if loaded or not. */
    private final AtomicBoolean initialized;

    /** Latch that marks if loading finished. */
    private final ReactiveLatch initializeLatch;

    /** The result of {@link #load()}. */
    private @MonotonicNonNull Mono<CommandResult> loadResult;

    /**
     * Initializes a new context.
     *
     * @param invocation The invocation that triggered execution.
     * @param parameters The command parameters.
     * @param access The validator to use for access checks.
     */
    protected ContextImpl( final Invocation invocation, final List<Parameter<?>> parameters,
            final AccessValidator access ) {

        this.parameters = List.copyOf( parameters );

        this.invocation = invocation;
        this.access = access;

        this.context = new HashMap<>();

        this.arguments = null;
        this.reply = null;
        this.initialized = new AtomicBoolean( false );
        this.initializeLatch = new ReactiveLatch();
        this.loadResult = null;

    }

    /**
     * Creates the initial reply manager.
     *
     * @return The initial reply manager.
     */
    protected abstract Mono<ReplyManager> makeReplyManager();

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
     * Parses an argument.
     *
     * @param <R> The raw argument type.
     * @param <T> The parsed argument type.
     * @param parameter The parameter specification.
     * @param getter The function to use to get the raw argument.
     * @param parser The parser to use.
     * @return A Mono that issues the parsed argument. May fail with a 
     *         {@link InvalidArgumentException} if the raw value is invalid, and may be empty
     *         if it is missing.
     * @throws InvalidArgumentException if the raw value is invalid.
     */
    @SideEffectFree
    private <R extends @NonNull Object, T extends @NonNull Object> Mono<T> parseArgument(
            final Parameter<T> parameter,
            final Function<String, Mono<R>> getter,
            final ArgumentParser<R, T> parser 
    ) throws InvalidArgumentException {

        return getter.apply( parameter.name() )
                .flatMap( raw -> parser.parse( this, raw ) );

    }

    /**
     * Parses a channel argument.
     *
     * @param <C> The channel type.
     * @param <T> The parsed argument type.
     * @param parameter The parameter specification.
     * @param parser The parser to use.
     * @return A Mono that issues the parsed argument. May fail with a 
     *         {@link InvalidArgumentException} if the raw value is invalid, and may be empty
     *         if it is missing.
     * @throws InvalidArgumentException if the raw value is invalid.
     * @apiNote Channel gets an additional wrapper in order to carry the C generic.
     */
    private <C extends @NonNull Channel, T extends @NonNull Object> Mono<T> parseArgument(
            final Parameter<T> parameter,
            final ChannelArgumentParser<C, T> parser
    ) throws InvalidArgumentException {

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
     *         {@link InvalidArgumentException} if the raw value is invalid, and may be empty
     *         if it is missing.
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
     * Handles a missing argument by using the default or issuing a result, as specified by
     * the parameter spec.
     *
     * @param <T> The argument type.
     * @param parameter The parameter whose argument is missing.
     * @return A Mono that issues the default value, or fails with a {@link ResultException}
     *         if the parameter is not allowed to be missing. May also be empty if there is
     *         no default and the spec allows it.
     */
    @SideEffectFree
    private <T extends @NonNull Object> Mono<T> handleMissingArgument( 
            final Parameter<T> parameter ) {

        if ( parameter.required() ) {
            final var result = new CommandFailureArgumentMissing( parameter );
            return Mono.error( new ResultException( result ) );
        } else {
            final var def = parameter.defaultValue();
            return def == null ? Mono.empty() : Mono.just( def );
        }

    }

    /**
     * Parses an argument, wrapping encountered invalid results and handling missing arguments.
     *
     * @param <T> The parsed argument type.
     * @param parameter The parameter to parse.
     * @return A Mono that issues the parsed argument. May fail with a {@link ResultException}
     *         if the raw value is invalid or is missing (and is required).
     */
    @SideEffectFree
    private <T extends @NonNull Object> Mono<? extends Argument<T>> parseArgumentWrapped( 
            final Parameter<T> parameter ) {

        try {
            final var ex = InvalidArgumentException.class;
            return parseArgument( parameter )
                    .onErrorMap( ex, e -> wrapInvalidParam( parameter, e ) )
                    .switchIfEmpty( Mono.defer( () -> handleMissingArgument( parameter ) ) )
                    .map( v -> new Argument<>( parameter, v ) )
                    .switchIfEmpty( Mono.fromSupplier( () -> new Argument<>( parameter, null ) ) );
        } catch ( final InvalidArgumentException e ) {
            return Mono.error( wrapInvalidParam( parameter, e ) );
        }
        
    }

    /**
     * Processes an argument.
     *
     * @param parameter The parameter specification.
     * @return A Mono that issues the parameter name and the processed argument. May fail 
     *         with a {@link ResultException} if the raw value is invalid or is missing 
     *         (and is required).
     */
    @SideEffectFree
    @SuppressWarnings( "return" ) // Bug with type inference in checker
    private Mono<? extends Entry<String, ? extends Argument<?>>> 
            processArgument( final Parameter<?> parameter ) {

        return parseArgumentWrapped( parameter )
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
    public Invocation getInvocation() {

        return invocation;

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
    public <T> @Nullable T getContext( final String key, final Class<? extends T> type )
            throws IllegalArgumentException, ClassCastException {

        if ( !context.containsKey( key ) ) {
            throw new IllegalArgumentException( String.format( 
                    "No context under key '%s'.", key ) );
        }

        return type.cast( context.get( key ) );

    }

    /**
     * @throws IllegalStateException if the context was not loaded yet.
     */
    @Override
    @Pure
    public ReplyManager replyManager() throws IllegalStateException {

        if ( reply == null ) {
            throw new IllegalStateException();
        } else {
            return reply;
        }

    }

    @Override
    public final Mono<Boolean> hasAccess( final Group group ) {

        return access.hasAccess( group );

    }

    @Override
    public final Mono<CommandResult> validate( final Group group ) {

        return access.validate( group );

    }

    @Override
    public Mono<Void> initialize() {

        if ( initialized.getAndSet( true ) ) {
            return initializeLatch.await(); // Already initializing
        }

        LOGGER.trace( "Initializing context" );

        // Prepare load
        // cache() for idempotence and to prevent cancelling
        this.loadResult = Mono.defer( this::doLoad ).cache();

        return makeReplyManager() // Initialize reply manager
                .map( ReplyManagerWrapper::new )
                .doOnNext( manager -> {
                    this.reply = manager;
                } )
                .then()
                .doOnSuccess( v -> initializeLatch.countDown() )
                .doOnError( initializeLatch::fail )
                .doOnSuccess( v -> LOGGER.trace( "Context initialized" ) )
                .doOnError( t -> LOGGER.error( "Failed to initialize", t ) )
                .cache(); // Prevent cancelling

    }

    /**
     * Performs the {@link #load()} operation.
     *
     * @return The result.
     * @see #load()
     */
    @EnsuresCalledMethods( value = "this", methods = "initArgs" )
    public Mono<CommandResult> doLoad() {

        LOGGER.trace( "Loading context" );

        return initArgs()
                .thenMany( Flux.fromIterable( parameters ) )
                .flatMap( this::processArgument )
                .collectMap( Entry::getKey, Entry::getValue )
                .doOnNext( args -> {
                    this.arguments = args;
                } )
                .name( "parameter-parse" ).metrics()
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
        public <E> @Nullable E getValue( final Class<E> argumentType ) throws ClassCastException {

            return argumentType.cast( value );

        }

    }

    /**
     * Wraps the reply manager for this context.
     *
     * @version 1.0
     * @since 1.0
     */
    private static final class ReplyManagerWrapper implements ReplyManager {

        /** The backing manager. */
        private ReplyManager backing;

        /**
         * Creates a new instance.
         *
         * @param backing The manager to wrap.
         */
        ReplyManagerWrapper( final ReplyManager backing ) {

            this.backing = backing;

        }

        @Override
        public ReplyManager setPrivate( final boolean priv ) {
            return backing.setPrivate( priv );
        }

        @Override
        public ReplyManager setEphemeral( final EphemeralType ephemeral ) {
            return backing.setEphemeral( ephemeral );
        }

        @Override
        public ReplyManager setDeleteDelay( final Duration delay ) {
            return backing.setDeleteDelay( delay );
        }

        @Override
        public Mono<Void> defer() {
            return backing.defer();
        }

        @Override
        public Mono<Void> reply( final MessageCreateSpec spec ) throws IllegalStateException {
            return backing.reply( spec );
        }

        @Override
        public Mono<Void> reply( final String content ) throws IllegalStateException {
            return backing.reply( content );
        }

        @Override
        public Mono<Void> reply( final EmbedCreateSpec... embeds ) throws IllegalStateException {
            return backing.reply( embeds );
        }

        @Override
        public Mono<Tuple2<Message, Integer>> add( final MessageCreateSpec spec ) {
            return backing.add( spec );
        }

        @Override
        public Mono<Tuple2<Message, Integer>> add( final String content ) {
            return backing.add( content );
        }

        @Override
        public Mono<Tuple2<Message, Integer>> add( final EmbedCreateSpec... embeds ) {
            return backing.add( embeds );
        }

        @Override
        public Mono<Message> edit( final MessageEditSpec spec ) throws IllegalStateException {
            return backing.edit( spec );
        }

        @Override
        public Mono<Message> edit( final String content ) throws IllegalStateException {
            return backing.edit( content );
        }

        @Override
        public Mono<Message> edit( final EmbedCreateSpec... embeds ) throws IllegalStateException {
            return backing.edit( embeds );
        }

        @Override
        public Mono<Message> edit( final int index, final MessageEditSpec spec )
                throws IndexOutOfBoundsException {
            return backing.edit( index, spec );
        }

        @Override
        public Mono<Message> edit( final int index, final String content ) 
                throws IndexOutOfBoundsException {
            return backing.edit( index, content );
        }

        @Override
        public Mono<Message> edit( final int index, final EmbedCreateSpec... embeds )
                throws IndexOutOfBoundsException {
            return backing.edit( index, embeds );
        }

        @Override
        public Mono<Message> get( final int index ) throws IndexOutOfBoundsException {
            return backing.get( index );
        }

        @Override
        public Mono<Message> get() throws IllegalStateException {
            return backing.get();
        }

        @Override
        public Mono<Void> delete( final int index ) throws IndexOutOfBoundsException {
            return backing.delete( index );
        }

        @Override
        public Mono<Void> delete() throws IllegalStateException {
            return backing.delete();
        }

        /**
         * Replaces the backing manager with its long-term manager.
         *
         * @return This manager, after replacing the backing manager.
         */
        @Override
        public synchronized ReplyManager longTerm() {

            backing = backing.longTerm();
            return this;

        }

    }
    
}
