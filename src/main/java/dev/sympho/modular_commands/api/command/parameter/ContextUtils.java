package dev.sympho.modular_commands.api.command.parameter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Utility functions for Context interfaces.
 *
 * @version 1.0
 * @since 1.0
 */
final class ContextUtils {

    /** Class should not be instantiated. */
    private ContextUtils() {}

    /**
     * Validates the choices specified for a parameter, and returns them as a new,
     * unmodifiable map.
     *
     * @param <T> The type of each choice.
     * @param choices The choices, or {@code null} if no restriction on value is
     *                to be used.
     * @return The choices, or an empty map if no restriction on value is to be used.
     * @throws IllegalArgumentException if the given map is empty or contains an empty
     *                                  choice name.
     * @throws NullPointerException if the choices contain {@code null}.
     */
    public static <T extends @NonNull Object> 
            Map<String, T> validateChoices( final @Nullable Map<String, T> choices )
            throws IllegalArgumentException {

        if ( choices == null ) {
            return Collections.emptyMap();
        } else {
            if ( choices.isEmpty() ) {
                throw new IllegalArgumentException( "Choices set cannot be empty." );
            } else if ( choices.keySet().stream().anyMatch( Objects::isNull ) ) {
                throw new NullPointerException( "One of the choice names was null." );
            } else if ( choices.values().stream().anyMatch( Objects::isNull ) ) {
                throw new NullPointerException( "One of the choices was null." );
            } else if ( choices.keySet().stream().anyMatch( String::isEmpty ) ) {
                throw new IllegalArgumentException( "A choice name cannot be empty." );
            }
            return Collections.unmodifiableMap( new HashMap<>( choices ) );
        }

    }

}
