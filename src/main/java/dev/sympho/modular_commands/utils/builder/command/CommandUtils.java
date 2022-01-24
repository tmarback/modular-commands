package dev.sympho.modular_commands.utils.builder.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import org.apache.commons.lang3.Range;
import org.checkerframework.checker.regex.qual.Regex;
import org.checkerframework.common.value.qual.StaticallyExecutable;
import org.checkerframework.dataflow.qual.Pure;

import dev.sympho.modular_commands.api.command.Command;
import dev.sympho.modular_commands.api.command.handler.CommandHandler;
import dev.sympho.modular_commands.api.command.handler.ResultHandler;
import dev.sympho.modular_commands.api.command.parameter.Parameter;
import discord4j.rest.util.PermissionSet;

/**
 * Utility functions for Commands.
 *
 * @version 1.0
 * @since 1.0
 */
public final class CommandUtils {

    /** Pattern for valid slash command names in the Discord API. */
    private static final @Regex String NAME_REGEX = "^[\\w-]{1,32}$";
    /** Compiled name pattern. */
    private static final Pattern NAME_PATTERN = Pattern.compile( 
            NAME_REGEX, Pattern.UNICODE_CHARACTER_CLASS );

    /** Pattern for valid user/message command names in the Discord API. */
    private static final @Regex String DISPLAY_NAME_REGEX = "^[\\w- ]{1,32}$";
    /** Compiled display name pattern. */
    private static final Pattern DISPLAY_NAME_PATTERN = Pattern.compile( 
            DISPLAY_NAME_REGEX, Pattern.UNICODE_CHARACTER_CLASS );

    /** The valid range for description length in the Discord API. */
    private static final Range<Integer> DESCRIPTION_RANGE = Range.between( 1, 100 );

    /** Class should not be instantiated. */
    private CommandUtils() {}

    /**
     * Validates the name of a command.
     *
     * @param name The name to validate.
     * @return The validated name.
     * @throws IllegalArgumentException if the name is not valid.
     */
    @Pure
    @StaticallyExecutable
    public static String validateName( final String name ) throws IllegalArgumentException {

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
     * Validates the display name of a command.
     *
     * @param name The display name to validate.
     * @return The validated display name.
     * @throws IllegalArgumentException if the display name is not valid.
     */
    @Pure
    @StaticallyExecutable
    public static String validateDisplayName( final String name ) 
            throws IllegalArgumentException {

        Objects.requireNonNull( name, "Display name cannot be null." );
        if ( !DISPLAY_NAME_PATTERN.matcher( name ).matches() ) {
            throw new IllegalArgumentException( "Invalid display name." );
        }
        return name;

    }

    /**
     * Validates the description of a command.
     *
     * @param description The description to validate.
     * @return The validated description.
     * @throws IllegalArgumentException if the description is not valid.
     */
    @Pure
    @StaticallyExecutable
    public static String validateDescription( final String description ) 
            throws IllegalArgumentException {

        Objects.requireNonNull( description, "Description cannot be null." );
        if ( DESCRIPTION_RANGE.contains( description.length() ) ) {
            throw new IllegalArgumentException( 
                    "Description must be between 1 and 100 characters." );
        }
        return description;

    }

    /**
     * Validates the parameters of a command.
     *
     * @param parameters The parameters to validate.
     * @param noRequired If {@code true}, does not accept parameters that are marked
     *                   as required.
     * @return An immutable copy of the validated parameter list, in the same order as given.
     * @throws IllegalArgumentException if the parameter list is not valid.
     */
    @Pure
    public static List<Parameter<?>> validateParameters( final List<Parameter<?>> parameters, 
            final boolean noRequired ) throws IllegalArgumentException {

        Objects.requireNonNull( parameters, "Parameter list cannot be null." );
        parameters.stream().forEach( p -> Objects.requireNonNull( p, 
                "Parameter specification cannot be null." ) );
        if ( noRequired && parameters.stream().anyMatch( Parameter::required ) ) {
            throw new IllegalArgumentException( "Required parameters not allowed." );
        }

        return Collections.unmodifiableList( new ArrayList<>( parameters ) );

    }

    /**
     * Validates the required built-in permissions of a command.
     *
     * @param perms The permission set to validate.
     * @return The validated permission set.
     */
    @Pure
    public static PermissionSet validateDiscordPermissions( final PermissionSet perms ) {

        Objects.requireNonNull( perms, "Discord permission set cannot be null." );

        return perms;

    }

    /**
     * Validates the invocation handler of a command.
     *
     * @param <H> The handler type.
     * @param handler The handler to validate.
     * @return The validated handler.
     */
    @Pure
    public static <H extends CommandHandler> H validateInvocationHandler( final H handler ) {

        Objects.requireNonNull( handler, "Invocation handler cannot be null." );

        return handler;

    }

    /**
     * Validates the result handlers of a command.
     *
     * @param <H> The handler type.
     * @param handlers The result handlers to validate.
     * @return An immutable copy of the validated handler list.
     */
    @Pure
    public static <H extends ResultHandler> List<H> validateResultHandlers( 
            final List<? extends H> handlers ) {

        Objects.requireNonNull( handlers, "Result handler list cannot be null." );
        handlers.stream().forEach( h -> Objects.requireNonNull( h, 
                "Result handler cannot be null." ) );

        return Collections.unmodifiableList( new ArrayList<>( handlers ) );

    }

    /**
     * Validates a command.
     *
     * @param <T> The command type.
     * @param command The command to validate.
     * @return The validated command.
     * @throws IllegalArgumentException If one of the components of the command was invalid.
     */
    @Pure
    public static <T extends Command> T validateCommand( final T command )
            throws IllegalArgumentException {

        validateName( command.name() );
        validateDisplayName( command.displayName() );
        validateDescription( command.description() );

        final boolean noRequired = false; // Detect User and Message commands later
        validateParameters( command.parameters(), noRequired );

        validateDiscordPermissions( command.requiredDiscordPermissions() );
        validateInvocationHandler( command.invocationHandler() );
        validateResultHandlers( command.resultHandlers() );

        return command;

    }

}
