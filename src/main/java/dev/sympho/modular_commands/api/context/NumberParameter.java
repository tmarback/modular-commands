package dev.sympho.modular_commands.api.context;

import org.checkerframework.checker.nullness.qual.NonNull;

import dev.sympho.modular_commands.api.exception.InvalidArgumentException;

/**
 * Specification for a parameter that receives a numeric value.
 *
 * @param <T> The parameter type.
 * @version 1.0
 * @since 1.0
 */
public sealed interface NumberParameter<T extends @NonNull Number & Comparable<T>>
        extends ChoicesParameter<T> 
        permits IntegerParameter, FloatParameter {

    /**
     * The minimum acceptable value (inclusive).
     *
     * @return The minimum value.
     */
    T minimum();
    
    /**
     * The maximum acceptable value (inclusive).
     *
     * @return The maximum value.
     */
    T maximum();

    /**
     * Parses the given raw argument from the user into the corresponding value.
     *
     * @param raw The raw argument received from the user.
     * @return The value specified by the argument.
     * @throws NumberFormatException if the given string is not a valid number.
     */
    T parseNumber( String raw ) throws NumberFormatException;

    @Override
    default T parseValue( String raw ) throws InvalidArgumentException {

        try {
            final T value = parseNumber( raw );
            if ( minimum().compareTo( value ) <= 0 && maximum().compareTo( value ) >= 0 ) {
                return value;
            } else {
                throw new InvalidArgumentException( this, 
                        String.format( "Value %s is not within the range [%s, %s].", 
                        raw, minimum().toString(), maximum().toString() ) );
            }
        } catch ( final NumberFormatException e ) {
            throw new InvalidArgumentException( this, 
                    String.format( "Value '%s' is not a valid number.", raw ), e );
        }

    }

}
