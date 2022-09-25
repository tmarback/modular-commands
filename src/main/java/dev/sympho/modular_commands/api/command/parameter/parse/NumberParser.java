package dev.sympho.modular_commands.api.command.parameter.parse;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;

/**
 * Parses number-based input arguments.
 *
 * @param <P> The type of number.
 * @param <T> The type of argument that is provided.
 * @version 1.0
 * @since 1.0
 * @apiNote Implementations do not have to verify in parsing that the received raw value
 *          is within the range specified by {@link #minimum()} and {@link #maximum()}.
 *          The system will automatically do this verification before invoking the parser.
 */
public sealed interface NumberParser<P extends @NonNull Number & Comparable<P>, 
        T extends @NonNull Object> extends ChoicesParser<P, T> permits IntegerParser, FloatParser {

    /**
     * The minimum value allowed (inclusive).
     *
     * @return The value, or {@code null} if no minimum.
     * @implSpec The default is {@code null}.
     */
    @Pure
    default @Nullable P minimum() {
        return null;
    }

    /**
     * The maximum value allowed (inclusive).
     *
     * @return The value, or {@code null} if no maximum.
     * @implSpec The default is {@code null}.
     */
    @Pure
    default @Nullable P maximum() {
        return null;
    }

    /**
     * Verifies that the given value is within the allowed range for this parser.
     *
     * @param value The value to validate.
     * @return The value.
     * @throws InvalidArgumentException If the value is outside the allowed range.
     */
    @Pure
    default P verifyInRange( final P value ) throws InvalidArgumentException {

        final var min = minimum();
        final var max = maximum();

        if ( min != null && value.compareTo( min ) < 0 ) {
            throw new InvalidArgumentException( value + " is below the minimum value of " + min );
        }

        if ( max != null && value.compareTo( max ) > 0 ) {
            throw new InvalidArgumentException( value + " is above the maximum value of " + max );
        }

        return value;

    }

    @Override
    default P validateRaw( final P raw ) throws InvalidArgumentException {
        return verifyInRange( ChoicesParser.super.validateRaw( raw ) );
    }
    
}
