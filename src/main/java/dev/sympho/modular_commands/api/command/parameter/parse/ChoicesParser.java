package dev.sympho.modular_commands.api.command.parameter.parse;

import java.util.List;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;

/**
 * Parses received input arguments, potentially restricting the acceptable values to a
 * set of choices.
 *
 * @param <P> The primitive type to be parsed.
 * @param <T> The type of argument that is provided.
 * @version 1.0
 * @since 1.0
 * @apiNote Implementations do not have to verify in parsing that the received raw value
 *          is one of the valid choices.
 *          The system will automatically do this verification before invoking the parser.
 */
public sealed interface ChoicesParser<P extends @NonNull Object, T extends @NonNull Object> 
        extends InputParser<P, T> permits StringParser, NumberParser {

    /** The maximum number of choices allowed. */
    int MAX_CHOICES = 25;

    /**
     * The allowed choices.
     *
     * @return The choices, or {@code null} if any value is allowed.
     *         Must have between 1 and {@value #MAX_CHOICES} elements if not {@code null}.
     */
    @Pure
    default @Nullable List<Choice<P>> choices() {
        return null;
    }

    /**
     * Verifies that the given value is one of the allowed choices.
     *
     * @param value The value to validate.
     * @return The value.
     * @throws InvalidArgumentException If the value is not a valid choice.
     */
    @Pure
    default P verifyChoice( final P value ) throws InvalidArgumentException {

        final var c = choices();
        if ( c != null && !c.stream().map( Choice::value ).anyMatch( value::equals ) ) {
            throw new InvalidArgumentException( "Not a valid choice" );
        }
        return value;

    }

    @Override
    default P validateRaw( final P raw ) throws InvalidArgumentException {
        return verifyChoice( InputParser.super.validateRaw( raw ) );
    }

    /**
     * A possible choice to be selected.
     *
     * @param <P> The raw value type.
     * @param name The choice name.
     * @param value The choice value.
     * @since 1.0
     */
    record Choice<P extends @NonNull Object>(
            String name,
            P value
    ) {

        /**
         * Creates a new choice.
         *
         * @param <P> The raw value type.
         * @param name The choice name.
         * @param value The choice value.
         * @return The choice.
         */
        static <P extends @NonNull Object> Choice<P> of( final String name, final P value ) {

            return new Choice<>( name, value );
    
        }

    }
    
}
