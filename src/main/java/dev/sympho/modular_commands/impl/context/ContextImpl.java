package dev.sympho.modular_commands.impl.context;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import com.google.common.collect.Streams;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.sympho.modular_commands.api.command.Invocation;
import dev.sympho.modular_commands.api.command.ReplyManager;
import dev.sympho.modular_commands.api.command.context.LazyContext;
import dev.sympho.modular_commands.api.command.parameter.Parameter;
import dev.sympho.modular_commands.api.command.result.CommandFailureArgumentExtra;
import dev.sympho.modular_commands.api.command.result.CommandFailureArgumentInvalid;
import dev.sympho.modular_commands.api.command.result.CommandFailureArgumentMissing;
import dev.sympho.modular_commands.api.exception.InvalidArgumentException;
import dev.sympho.modular_commands.execute.ResultException;
import dev.sympho.modular_commands.utils.ReactiveLatch;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.core.spec.MessageEditSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

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
    private final List<Parameter<?>> parameterOrder;
    /** The raw arguments in the order that they were received. */
    private final List<A> rawArguments;

    /** The parsed arguments. */
    private final Map<String, Argument> arguments;

    /** The invocation that triggered this context. */
    private final Invocation invocation;

    /** Storage for context objects. */
    private final Map<String, @Nullable Object> context;

    /** The reply manager. */
    private @MonotonicNonNull ReplyManager reply;

    /** Marks if loaded or not. */
    private final AtomicBoolean loaded;

    /** Latch that marks if loading finished. */
    private final ReactiveLatch loadLatch;

    /**
     * Initializes a new context.
     *
     * @param invocation The invocation that triggered execution.
     * @param parameters The command parameters.
     * @param rawArguments The raw arguments received.
     */
    protected ContextImpl( final Invocation invocation, final List<Parameter<?>> parameters,
            final List<A> rawArguments ) {

        this.parameterOrder = parameters.stream().toList();
        this.rawArguments = rawArguments.stream().toList();

        this.invocation = invocation;

        this.arguments = parameters.stream().collect( Collectors.toUnmodifiableMap( 
                    Parameter::name, p -> new Argument() ) );
        this.context = new HashMap<>();

        this.reply = null;
        this.loaded = new AtomicBoolean( false );
        this.loadLatch = new ReactiveLatch();

    }

    /**
     * Creates the initial reply manager.
     *
     * @return The initial reply manager.
     */
    protected abstract Mono<ReplyManager> makeReplyManager();

    /**
     * Parses an argument.
     *
     * @param parameter The parameter specification.
     * @param raw The raw argument.
     * @return A Mono that issues the parsed argument. If the raw value is invalid, it may 
     *         fail with a {@link InvalidArgumentException}.
     * @throws InvalidArgumentException if the raw value was invalid.
     */
    protected abstract Mono<Object> parseArgument( Parameter<?> parameter, A raw ) 
            throws InvalidArgumentException;

    /**
     * Converts a raw argument into a string.
     *
     * @param raw The argument.
     * @return The argument as a string.
     * @apiNote This is used to make error messages in some cases.
     * @implSpec The default just delegates to {@link Objects#toString(Object)}.
     */
    protected String rawToString( final A raw ) {
        return Objects.toString( raw );
    }

    /**
     * Wraps the error for an invalid parameter into a result.
     *
     * @param parameter The parameter.
     * @param raw The offending value.
     * @param exception The exeception that was caused.
     * @return The wrapped result.
     */
    private ResultException wrapInvalidParam( final Parameter<?> parameter, final A raw, 
            final InvalidArgumentException exception ) {

        LOGGER.trace( "Invalid argument {} for parameter {}", raw, parameter );
        final var arg = rawToString( raw );
        final var error = exception.getMessage();
        final var result = new CommandFailureArgumentInvalid( arg, parameter, error );
        return new ResultException( result );

    }
    
    /**
     * Parses an argument, wrapping errors into a result (both in the method itself and
     * in the resulting Mono).
     *
     * @param parameter The parameter specification.
     * @param raw The raw argument.
     * @return A Mono that issues the parsed argument. If the raw value is invalid, it may 
     *         fail with a {@link ResultException}.
     * @throws ResultException if the raw value was invalid.
     */
    private Mono<Object> parseArgumentWrapped( final Parameter<?> parameter, final A raw ) 
            throws ResultException {

        try {
            return parseArgument( parameter, raw ).onErrorMap( InvalidArgumentException.class, 
                    e -> wrapInvalidParam( parameter, raw, e ) );
        } catch ( final InvalidArgumentException e ) {
            throw wrapInvalidParam( parameter, raw, e );
        }
        
    }

    /**
     * Handles a parameter that did not receive a corresponding argument.
     *
     * @param parameter The parameter specification.
     * @return The default value if the parameter has one, otherwise {@code null}.
     * @throws ResultException if the parameter is required.
     */
    @Pure
    private static Optional<Object> missingArgument( final Parameter<?> parameter ) 
            throws ResultException {

        if ( parameter.required() ) {
            throw new ResultException( new CommandFailureArgumentMissing( parameter ) );
        }
        return Optional.ofNullable( parameter.defaultValue() );

    }

    @Override
    public Invocation getInvocation() {

        return invocation;

    }

    @Override
    public <T> @Nullable T getArgument( final String name, final Class<? extends T> argumentType )
            throws IllegalArgumentException, ClassCastException {

        if ( !arguments.containsKey( name ) ) {
            throw new IllegalArgumentException( String.format( "No parameter named '%s'.", name ) );
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
    public ReplyManager replyManager() throws IllegalStateException {

        if ( reply == null ) {
            throw new IllegalStateException();
        } else {
            return reply;
        }

    }

    @Override
    public Mono<Void> load() throws ResultException {

        if ( loaded.getAndSet( true ) ) {
            return loadLatch.await(); // Already loading
        }

        LOGGER.trace( "Parsing arguments {} for parameters {}", rawArguments, parameterOrder );

        final var received = rawArguments.size();
        final var expected = parameterOrder.size();
        if ( received > expected ) {
            final var unexpected = rawArguments.stream()
                    .skip( expected )
                    .map( this::rawToString )
                    .toList();
            throw new ResultException( new CommandFailureArgumentExtra( unexpected ) );
        }

        final var parsed = Streams.zip( parameterOrder.stream(), rawArguments.stream(),
                this::parseArgumentWrapped );
        final var missing = parameterOrder.stream()
                .skip( received )
                .map( ContextImpl::missingArgument );

        return Flux.concat( parsed.toList() )
                .map( Optional::of )
                .concatWith( Flux.fromStream( missing ) )
                .zipWithIterable( parameterOrder )
                .map( t -> Tuples.of( t.getT2().name(), t.getT1() ) )
                .map( t -> Tuples.of( arguments.get( t.getT1() ), t.getT2() ) )
                .doOnNext( t -> t.getT1().setValue( t.getT2().orElse( null ) ) )
                .name( "parameter-parse" ).metrics()
                .then( makeReplyManager() ) // Initialize reply manager
                .map( ReplyManagerWrapper::new )
                .doOnNext( manager -> {
                    this.reply = manager;
                } )
                .doOnSuccess( m -> loadLatch.countDown() )
                .doOnError( loadLatch::fail )
                .then();

    }

    /**
     * An argument that corresponds to one of the command's formal parameters.
     *
     * @version 1.0
     * @since 1.0
     */
    private static class Argument {

        /** The argument value. */
        private @Nullable Object value;

        /** Creates a new instance. */
        Argument() {}

        /**
         * Retrieves the argument value.
         *
         * @param <T> The value type.
         * @param argumentType The value type.
         * @return The value.
         * @throws ClassCastException if the value is not compatible with the given type.
         */
        public <T> @Nullable T getValue( final Class<? extends T> argumentType )
                throws ClassCastException {

            return argumentType.cast( value );

        }

        /**
         * Sets the argument value.
         *
         * @param value The value to set.
         */
        public void setValue( final @Nullable Object value ) {

            this.value = value;

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
