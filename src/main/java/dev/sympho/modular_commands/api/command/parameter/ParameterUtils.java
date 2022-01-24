package dev.sympho.modular_commands.api.command.parameter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.common.value.qual.StaticallyExecutable;
import org.checkerframework.dataflow.qual.Pure;

import dev.sympho.modular_commands.utils.builder.command.CommandUtils;

/**
 * Utility functions for Parameter interfaces.
 *
 * @version 1.0
 * @since 1.0
 */
public final class ParameterUtils {

    /** Class should not be instantiated. */
    private ParameterUtils() {}

    /**
     * Validates the name of a parameter.
     *
     * @param name The name to validate.
     * @return The validated name.
     * @throws IllegalArgumentException if the name is not valid.
     */
    @Pure
    @StaticallyExecutable
    public static String validateName( final String name ) throws IllegalArgumentException {

        return CommandUtils.validateName( name );

    }

    /**
     * Validates the description of a parameter.
     *
     * @param description The description to validate.
     * @return The validated description.
     * @throws IllegalArgumentException if the description is not valid.
     */
    @Pure
    @StaticallyExecutable
    public static String validateDescription( final String description ) 
            throws IllegalArgumentException {

        return CommandUtils.validateDescription( description );

    }

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
    @Pure
    public static <T extends @NonNull Object> 
            Map<String, T> validateChoices( final @Nullable Map<String, T> choices )
            throws IllegalArgumentException {

        if ( choices == null ) {
            return Collections.emptyMap();
        } else if ( choices.isEmpty() ) {
            throw new IllegalArgumentException( "Choices set cannot be empty." );
        } else {
            choices.entrySet().forEach( e -> {
                Objects.requireNonNull( e.getKey(), "One of the choice names was null." );
                Objects.requireNonNull( e.getValue(), "One of the choices was null." );
                if ( e.getKey().isEmpty() ) {
                    throw new IllegalArgumentException( "A choice name cannot be empty." );
                }
            } );

            return Collections.unmodifiableMap( new HashMap<>( choices ) );
        }

    }

}
