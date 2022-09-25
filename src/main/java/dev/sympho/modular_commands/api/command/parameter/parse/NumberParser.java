package dev.sympho.modular_commands.api.command.parameter.parse;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

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
     * @throws InvalidArgumentException If the value is outside the allowed range.
     * @apiNote Implementations do not need to call this method.
     */
    @SideEffectFree
    default void verifyInRange( final P value ) throws InvalidArgumentException {

        final var min = minimum();
        final var max = maximum();

        if ( min != null && value.compareTo( min ) < 0 ) {
            throw new InvalidArgumentException( value + " is below the minimum value of " + min );
        }

        if ( max != null && value.compareTo( max ) > 0 ) {
            throw new InvalidArgumentException( value + " is above the maximum value of " + max );
        }

    }

    /**
     * The raw type.
     *
     * @return The type.
     * @apiNote This method exists only for {@link #verifyInRangeCast(Object)}.
     */
    @Pure
    Class<P> rawType();

    /**
     * Verifies that the given value is one of the allowed choices, casting it to the
     * raw type first.
     *
     * @param value The value to validate.
     * @throws InvalidArgumentException If the value is not a valid choice.
     * @throws ClassCastException If given value is not of the correct type.
     * @apiNote This method should be avoided as much as possible. It exists only because
     *          Generics interacts incredibly poorly with {@code instanceof} and everything
     *          is a compiler error that can't be overriden.
     */
    default void verifyInRangeCast( final Object value ) 
            throws ClassCastException, InvalidArgumentException {
        verifyInRange( rawType().cast( value ) );
    }
    
}
