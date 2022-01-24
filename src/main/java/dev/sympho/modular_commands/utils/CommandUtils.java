package dev.sympho.modular_commands.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.Range;
import org.checkerframework.checker.regex.qual.Regex;
import org.checkerframework.common.value.qual.StaticallyExecutable;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.Command;
import dev.sympho.modular_commands.api.command.Invocation;
import dev.sympho.modular_commands.api.command.MessageCommand;
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
     * Validates the parent of a command.
     *
     * @param parent The parent to validate.
     * @return The validated parent.
     */
    public static Invocation validateParent( final Invocation parent ) {

        Objects.requireNonNull( parent, "Parent cannot be null." );
        parent.chain().forEach( CommandUtils::validateName );
        return parent;

    }

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
     * Validates the aliases of a command.
     *
     * @param aliases The aliases to validate.
     * @return An immutable copy of the validated alias set.
     * @throws IllegalArgumentException if the alias set is not valid.
     */
    @SideEffectFree
    public static Set<String> validateAliases( final Set<String> aliases ) 
            throws IllegalArgumentException {

        Objects.requireNonNull( aliases, "Alias set cannot be null." );
        aliases.forEach( a -> {
            Objects.requireNonNull( a, "Alias cannot be null." );
            if ( !NAME_PATTERN.matcher( a ).matches() ) {
                throw new IllegalArgumentException( "Invalid alias." );
            }
        } );
        return Collections.unmodifiableSet( new HashSet<>( aliases ) );

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
    @SideEffectFree
    public static List<Parameter<?>> validateParameters( final List<Parameter<?>> parameters, 
            final boolean noRequired ) throws IllegalArgumentException {

        Objects.requireNonNull( parameters, "Parameter list cannot be null." );
        boolean optional = false;
        for ( final Parameter<?> p : parameters ) {
            Objects.requireNonNull( p, "Parameter specification cannot be null." );
            if ( p.required() ) {
                if ( noRequired ) {
                    throw new IllegalArgumentException( "Required parameters not allowed." );
                } else if ( optional ) {
                    throw new IllegalArgumentException( 
                            "Required parameters must be before optional parameters." );
                }
            } else {
                optional = true;
            }
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

        return Objects.requireNonNull( perms, "Discord permission set cannot be null." );

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

        return Objects.requireNonNull( handler, "Invocation handler cannot be null." );

    }

    /**
     * Validates the result handlers of a command.
     *
     * @param <H> The handler type.
     * @param handlers The result handlers to validate.
     * @return An immutable copy of the validated handler list.
     */
    @SideEffectFree
    public static <H extends ResultHandler> List<H> validateResultHandlers( 
            final List<? extends H> handlers ) {

        Objects.requireNonNull( handlers, "Result handler list cannot be null." );
        handlers.forEach( h -> Objects.requireNonNull( h, "Result handler cannot be null." ) );

        return Collections.unmodifiableList( new ArrayList<>( handlers ) );

    }

    /**
     * Validates a command.
     *
     * @param <C> The command type.
     * @param command The command to validate.
     * @return The validated command.
     * @throws IllegalArgumentException If one of the components of the command was invalid.
     */
    @Pure
    public static <C extends Command> C validateCommand( final C command )
            throws IllegalArgumentException {

        validateName( command.name() );
        validateDisplayName( command.displayName() );
        validateDescription( command.description() );

        final boolean noRequired = false; // Detect User and Message commands later
        validateParameters( command.parameters(), noRequired );

        validateDiscordPermissions( command.requiredDiscordPermissions() );
        validateInvocationHandler( command.invocationHandler() );
        validateResultHandlers( command.resultHandlers() );

        if ( command instanceof MessageCommand c ) {
            validateAliases( c.aliases() );
        }

        return command;

    }

}
