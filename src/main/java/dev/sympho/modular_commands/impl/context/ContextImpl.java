package dev.sympho.modular_commands.impl.context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.collect.Streams;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;

import dev.sympho.modular_commands.api.command.Invocation;
import dev.sympho.modular_commands.api.command.context.LazyContext;
import dev.sympho.modular_commands.api.command.parameter.Parameter;
import dev.sympho.modular_commands.api.command.result.CommandFailureArgumentExtra;
import dev.sympho.modular_commands.api.command.result.CommandFailureArgumentInvalid;
import dev.sympho.modular_commands.api.command.result.CommandFailureArgumentMissing;
import dev.sympho.modular_commands.api.exception.InvalidArgumentException;
import dev.sympho.modular_commands.execute.ResultException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

/**
 * Base implementation for context objects.
 *
 * @param <A> The type that raw arguments are received in.
 * @version 1.0
 * @since 1.0
 */
abstract class ContextImpl<A extends @NonNull Object> implements LazyContext {

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

    }

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

    @Override
    public Mono<Void> load() throws ResultException {

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
                .skip( arguments.size() )
                .map( ContextImpl::missingArgument );

        return Flux.concat( parsed.toList() )
                .map( Optional::of )
                .concatWith( Flux.fromStream( missing ) )
                .zipWithIterable( parameterOrder )
                .map( t -> Tuples.of( t.getT2().name(), t.getT1() ) )
                .map( t -> Tuples.of( arguments.get( t.getT1() ), t.getT2() ) )
                .doOnNext( t -> t.getT1().setValue( t.getT2().orElse( null ) ) )
                .then()
                .name( "parameter-parse" ).metrics();

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
    
}
