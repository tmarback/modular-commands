package dev.sympho.modular_commands.impl.context;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.sympho.modular_commands.api.command.Invocation;
import dev.sympho.modular_commands.api.command.ReplyManager;
import dev.sympho.modular_commands.api.command.context.LazyContext;
import dev.sympho.modular_commands.api.command.parameter.AttachmentParameter;
import dev.sympho.modular_commands.api.command.parameter.InputParameter;
import dev.sympho.modular_commands.api.command.parameter.Parameter;
import dev.sympho.modular_commands.api.command.parameter.parse.InvalidArgumentException;
import dev.sympho.modular_commands.api.command.result.CommandFailureArgumentInvalid;
import dev.sympho.modular_commands.api.command.result.CommandFailureArgumentMissing;
import dev.sympho.modular_commands.api.command.result.CommandResult;
import dev.sympho.modular_commands.api.exception.ResultException;
import dev.sympho.modular_commands.api.permission.AccessValidator;
import dev.sympho.modular_commands.api.permission.Group;
import dev.sympho.modular_commands.utils.ReactiveLatch;
import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.Message;
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

    /** An argument that is missing. */
    private static final Argument ARG_MISSING = new Argument( null );

    /** The command parameters in the order that they should be received. */
    private final List<? extends Parameter<?, ?>> parameters;

    /** The invocation that triggered this context. */
    private final Invocation invocation;

    /** The validator to use for access checks. */
    private final AccessValidator access;

    /** Storage for context objects. */
    private final Map<String, @Nullable Object> context;

    /** The parsed arguments. */
    private @MonotonicNonNull Map<String, Argument> arguments;

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
    protected ContextImpl( final Invocation invocation, final List<Parameter<?, ?>> parameters,
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

    /**
     * Retrieves the input argument associated with the parameter of the given name.
     *
     * @param name The parameter name.
     * @return The associated input argument.
     */
    protected abstract @Nullable A getInputArgument( String name );

    /**
     * Retrieves the attachment argument associated with the parameter of the given name.
     *
     * @param name The parameter name.
     * @return The associated attachment argument.
     */
    protected abstract @Nullable Attachment getAttachmentArgument( String name );

    /**
     * Parses an input argument.
     *
     * @param <T> The argument type.
     * @param parameter The parameter specification.
     * @param raw The raw argument.
     * @return A Mono that issues the parsed argument. If the raw value is invalid, it may 
     *         fail with a {@link InvalidArgumentException}.
     * @throws InvalidArgumentException if the raw value was invalid.
     */
    protected abstract <T extends @NonNull Object> Mono<T> parseInputArgument( 
            InputParameter<T> parameter, A raw ) 
            throws InvalidArgumentException;

    /**
     * Parses an attachment argument.
     *
     * @param <T> The argument type.
     * @param parameter The parameter specification.
     * @param raw The raw argument.
     * @return A Mono that issues the parsed argument. If the raw value is invalid, it may 
     *         fail with a {@link InvalidArgumentException}.
     * @throws InvalidArgumentException if the raw value was invalid.
     * @implSpec Delegates to the parameter itself.
     */
    protected <T extends @NonNull Object> Mono<T> parseAttachmentArgument( 
            final AttachmentParameter<T> parameter, final Attachment raw ) 
            throws InvalidArgumentException {

        return parameter.parse( this, raw );
        
    }

    /**
     * Wraps the error for an invalid parameter into a result.
     *
     * @param parameter The parameter.
     * @param exception The exeception that was caused.
     * @return The wrapped result.
     */
    @SideEffectFree
    private ResultException wrapInvalidParam( final Parameter<?, ?> parameter,
            final InvalidArgumentException exception ) {

        LOGGER.trace( "Invalid argument for parameter {}: {}", parameter, exception.getMessage() );
        final var error = exception.getMessage();
        final var result = new CommandFailureArgumentInvalid( parameter, error );
        return new ResultException( result );

    }
    
    /**
     * Parses an argument, wrapping errors into a result (both in the method itself and
     * in the resulting Mono).
     *
     * @param <R> The raw argument type.
     * @param <T> The parsed argument type.
     * @param <P> The parameter type.
     * @param parameter The parameter specification.
     * @param argument The raw argument.
     * @param parser The parser to use.
     * @return A Mono that issues the parsed argument. If the raw value is invalid, it may 
     *         fail with a {@link ResultException}.
     */
    @SideEffectFree
    private <R, T extends @NonNull Object, P extends Parameter<T, ?>> Mono<T> parseArgumentWrapped(
                final P parameter, final R argument, final BiFunction<P, R, Mono<T>> parser ) {

        try {
            return parser.apply( parameter, argument ).onErrorMap( InvalidArgumentException.class,
                    e -> wrapInvalidParam( parameter, e ) );
        } catch ( final InvalidArgumentException e ) {
            return Mono.error( wrapInvalidParam( parameter, e ) );
        }
        
    }

    /**
     * Parses an argument.
     *
     * @param <R> The raw argument type.
     * @param <T> The parsed argument type.
     * @param <P> The parameter type.
     * @param parameter The parameter specification.
     * @param argumentGetter The function to use to get the raw argument.
     * @param parser The parser to use.
     * @return A Mono that issues the parsed argument. May fail with a {@link ResultException}
     *         if the raw value is invalid or is missing (and is required), and may be empty
     *         if the argument was not provided (and is not required and has no default).
     */
    @SideEffectFree
    private <R, T extends @NonNull Object, P extends Parameter<T, ?>> Mono<T> parseArgument( 
            final P parameter, 
            final Function<String, @Nullable R> argumentGetter, 
            final BiFunction<P, R, Mono<T>> parser ) {

        final var raw = argumentGetter.apply( parameter.name() );
        if ( raw != null ) {
            return parseArgumentWrapped( parameter, raw, parser );
        } else if ( parameter.required() ) {
            final var result = new CommandFailureArgumentMissing( parameter );
            return Mono.error( new ResultException( result ) );
        } else {
            final var def = parameter.defaultValue();
            return def == null ? Mono.empty() : Mono.just( def );
        }

    }

    /**
     * Parses an input argument.
     *
     * @param <T> The argument type.
     * @param parameter The parameter specification.
     * @return A Mono that issues the parsed argument. May fail with a {@link ResultException}
     *         if the raw value is invalid or is missing (and is required), and may be empty
     *         if the argument was not provided (and is not required and has no default).
     */
    @SideEffectFree
    private <T extends @NonNull Object> Mono<T> parseArgument( 
                final InputParameter<T> parameter ) {

        return parseArgument( parameter, 
                this::getInputArgument, 
                this::parseInputArgument 
        );

    }

    /**
     * Parses an attachment argument.
     *
     * @param <T> The argument type.
     * @param parameter The parameter specification.
     * @return A Mono that issues the parsed argument. May fail with a {@link ResultException}
     *         if the raw value is invalid or is missing (and is required), and may be empty
     *         if the argument was not provided (and is not required and has no default).
     */
    @SideEffectFree
    private <T extends @NonNull Object> Mono<T> parseArgument( 
                final AttachmentParameter<T> parameter ) {

        return parseArgument( parameter, 
                this::getAttachmentArgument, 
                this::parseAttachmentArgument 
        );

    }

    /**
     * Parses an argument of any parameter type.
     *
     * @param <T> The argument type.
     * @param parameter The parameter specification.
     * @return A Mono that issues the parsed argument. May fail with a {@link ResultException}
     *         if the raw value is invalid or is missing (and is required), and may be empty
     *         if the argument was not provided (and is not required and has no default).
     */
    @SideEffectFree
    @SuppressWarnings( { "JavadocMethod", "unchecked" } ) // Ignore undeclared exception
    private <T extends @NonNull Object> Mono<T> parseArgumentAny( 
            final Parameter<T, ?> parameter ) {

        // Note: Cannot use <T> directly in the instanceof because Generics
        // is horribly limited and can't even realize that a Parameter<T, ?> that is an 
        // instance of InputParameter is, by definition, an instance of InputParameter<T> 
        // (same for AttachmentParameter).
        if ( parameter instanceof InputParameter<?> p ) {
            return parseArgument( ( InputParameter<T> ) p );
        } else if ( parameter instanceof AttachmentParameter<?> p ) {
            return parseArgument( ( AttachmentParameter<T> ) p );
        } else {
            // Sanity check rather than an expected error so not declared
            throw new IllegalArgumentException( "Unrecognized parameter type" );
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
    private Mono<Entry<String, Argument>> processArgument( final Parameter<?, ?> parameter ) {

        return parseArgumentAny( parameter )
                .map( Argument::new )
                .defaultIfEmpty( ARG_MISSING )
                .map( a -> Map.entry( parameter.name(), a ) )
                .doOnError( t -> {
                    if ( t instanceof ResultException ex ) {
                        LOGGER.trace( "Arg processing aborted: {}", ex.getResult() );
                    } else {
                        LOGGER.error( "Failed to process argument", t );
                    }
                } );

    }

    @Override
    public Invocation getInvocation() {

        return invocation;

    }

    @Override
    public <T extends @NonNull Object> @Nullable T getArgument( 
            final String name, final Class<T> argumentType )
            throws IllegalArgumentException, ClassCastException {

        if ( arguments == null ) {
            throw new IllegalStateException( "Context not loaded yet" );
        }

        if ( !arguments.containsKey( name ) ) {
            throw new IllegalArgumentException( String.format( "No parameter named '%s'", name ) );
        }

        return arguments.get( name ).getValue( argumentType );

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
    @SideEffectFree
    public Mono<CommandResult> validate( final Group group ) {

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
    public Mono<CommandResult> doLoad() {

        LOGGER.trace( "Loading context" );

        return Flux.fromIterable( parameters )
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
     * @param value The argument value.
     * @version 1.0
     * @since 1.0
     */
    private record Argument( @Nullable Object value ) {

        /**
         * Retrieves the argument value.
         *
         * @param <T> The value type.
         * @param argumentType The value type.
         * @return The value.
         * @throws ClassCastException if the value is not compatible with the given type.
         */
        public <T> @Nullable T getValue( final Class<T> argumentType ) throws ClassCastException {

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
