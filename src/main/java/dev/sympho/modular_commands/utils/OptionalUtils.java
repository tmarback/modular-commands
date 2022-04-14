package dev.sympho.modular_commands.utils;

import java.util.Optional;

import org.checkerframework.checker.optional.qual.Present;

/**
 * Tools for dealing with OptionalChecker, similar to NullnessUtils.
 *
 * @version 1.0
 * @since 1.0
 */
public final class OptionalUtils {

    /**
     * Do not instantiate.
     */
    private OptionalUtils() {}

    /**
     * Casts an optional as present.
     *
     * @param <T> The object type.
     * @param o The Optional to cast.
     * @return The cast Optional.
     */
    @SuppressWarnings( { "return", "optional.parameter" } )
    public static <T> @Present Optional<T> castPresent( final Optional<T> o ) {

        return o;

    }
    
}
