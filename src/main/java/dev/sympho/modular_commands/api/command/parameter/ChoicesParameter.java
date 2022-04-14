package dev.sympho.modular_commands.api.command.parameter;

import java.util.Map;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.context.CommandContext;
import dev.sympho.modular_commands.api.exception.InvalidArgumentException;
import reactor.core.publisher.Mono;

/**
 * Specification for a parameter whose value may optionally be restricted
 * to a set of predetermined values.
 *
 * @param <T> The parameter type.
 * @version 1.0
 * @since 1.0
 */
public sealed interface ChoicesParameter<T extends @NonNull Object> extends Parameter<T> 
        permits NumberParameter, StringParameter {

    /**
     * The possible choices for the parameter value.
     * 
     * <p>If the returned map is empty, the parameter value is not
     * restricted to a set of choices (but may have other restrictions).
     *
     * @return The choices.
     */
    @Pure
    Map<String, T> choices();

    /**
     * Parses the given raw argument from the user into the corresponding value.
     *
     * @param raw The raw argument received from the user.
     * @return The value specified by the argument.
     * @throws InvalidArgumentException if the given string is not a valid value.
     */
    @SideEffectFree
    T parseValue( String raw ) throws InvalidArgumentException;

    @Override
    default Mono<T> parse( CommandContext context, String raw ) throws InvalidArgumentException {

        final T value = parseValue( raw );
        if ( choices().isEmpty() || choices().containsValue( value ) ) {
            return Mono.just( value );
        } else {
            throw new InvalidArgumentException( this, 
                    String.format( "Value '%s' is not a valid choice.", raw ) );
        }

    }
    
}
