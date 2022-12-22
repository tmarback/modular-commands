package dev.sympho.modular_commands.utils;

import java.util.Objects;
import java.util.regex.Pattern;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.common.value.qual.MatchesRegex;
import org.checkerframework.common.value.qual.StaticallyExecutable;
import org.checkerframework.dataflow.qual.Pure;

import dev.sympho.modular_commands.api.command.parameter.Parameter;

/**
 * Utility functions for Parameter interfaces.
 *
 * @version 1.0
 * @since 1.0
 */
public final class ParameterUtils {

    /** Compiled name pattern. */
    private static final Pattern NAME_PATTERN = Pattern.compile( 
            Parameter.NAME_REGEX );

    /** Compiled description pattern. */
    private static final Pattern DESCRIPTION_PATTERN = Pattern.compile( 
            Parameter.DESCRIPTION_REGEX );

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
    public static @MatchesRegex( Parameter.NAME_REGEX ) String validateName( 
            final @MatchesRegex( Parameter.NAME_REGEX ) String name ) 
            throws IllegalArgumentException {

        Objects.requireNonNull( name, "Name cannot be null." );
        if ( !NAME_PATTERN.matcher( name ).matches() ) {
            throw new IllegalArgumentException( "Invalid name." );
        }
        if ( !name.equals( name.toLowerCase() ) ) {
            throw new IllegalArgumentException( "Name must be all lowercase." );
        }
        return name;

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
    public static @MatchesRegex( Parameter.DESCRIPTION_REGEX ) String validateDescription( 
            final @MatchesRegex( Parameter.DESCRIPTION_REGEX ) String description ) 
            throws IllegalArgumentException {

        Objects.requireNonNull( description, "Description cannot be null." );
        if ( !DESCRIPTION_PATTERN.matcher( description ).matches() ) {
            throw new IllegalArgumentException( "Invalid description." );
        }
        return description;

    }

    /**
     * Validates a parameter.
     *
     * @param <T> The argument type.
     * @param parameter The parameter to validate.
     * @return The validated parameter.
     * @throws IllegalArgumentException if the parameter is not valid.
     */
    @Pure
    public static <T extends @NonNull Object> Parameter<T> validate( 
            final Parameter<T> parameter ) throws IllegalArgumentException {

        validateName( parameter.name() );
        validateDescription( parameter.description() );
        Objects.requireNonNull( parameter.parser(), "Parser cannot be null." );

        return parameter;

    }

}
