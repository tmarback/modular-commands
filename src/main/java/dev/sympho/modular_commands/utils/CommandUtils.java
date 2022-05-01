package dev.sympho.modular_commands.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMultiset;

import org.apache.commons.lang3.Range;
import org.checkerframework.checker.regex.qual.Regex;
import org.checkerframework.common.value.qual.StaticallyExecutable;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.Command;
import dev.sympho.modular_commands.api.command.Invocation;
import dev.sympho.modular_commands.api.command.MessageCommand;
import dev.sympho.modular_commands.api.command.handler.InvocationHandler;
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
     * Validates an alias.
     *
     * @param alias The alias to validate.
     * @return The validated alias.
     * @throws IllegalArgumentException if the alias is not valid.
     */
    @Pure
    @StaticallyExecutable
    public static String validateAlias( final String alias ) 
            throws IllegalArgumentException {

        Objects.requireNonNull( alias, "Alias cannot be null." );
        if ( !NAME_PATTERN.matcher( alias ).matches() ) {
            throw new IllegalArgumentException( "Invalid alias." );
        }
        return alias;

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

        return Objects.requireNonNull( aliases, "Alias set cannot be null." ).stream()
                .map( CommandUtils::validateAlias )
                .collect( Collectors.toUnmodifiableSet() );

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
        if ( !DESCRIPTION_RANGE.contains( description.length() ) ) {
            throw new IllegalArgumentException( 
                    "Description must be between 1 and 100 characters." );
        }
        return description;

    }

    /**
     * Validates the parameters of a command.
     *
     * @param parameters The parameters to validate.
     * @return An immutable copy of the validated parameter list, in the same order as given.
     * @throws IllegalArgumentException if the parameter list is not valid.
     */
    @SideEffectFree
    public static List<Parameter<?>> validateParameters( final List<Parameter<?>> parameters )
            throws IllegalArgumentException {

        Objects.requireNonNull( parameters, "Parameter list cannot be null." );
        boolean optional = false;
        for ( final Parameter<?> p : parameters ) {

            Objects.requireNonNull( p, "Parameter specification cannot be null." );
            if ( !p.required() ) {
                optional = true;
            } else if ( optional ) {
                throw new IllegalArgumentException( 
                        "Required parameters must be before optional parameters." );
            }

        }

        final var nameStream = parameters.stream().map( Parameter::name );
        final var names = ImmutableMultiset.copyOf( nameStream.iterator() );
        for ( final var entry : names.entrySet() ) {

            if ( entry.getCount() > 1 ) {
                throw new IllegalArgumentException( String.format( "Duplicate argument: %s",
                        entry.getElement() ) );
            }

        }

        return Collections.unmodifiableList( new ArrayList<>( parameters ) );

    }

    /**
     * Validates the required permissions of a command.
     *
     * @param perms The permission set to validate.
     * @return The validated permission set.
     */
    @Pure
    public static PermissionSet validatePermissions( final PermissionSet perms ) {

        return Objects.requireNonNull( perms, "Permission set cannot be null." );

    }

    /**
     * Validates the invocation handler of a command.
     *
     * @param <H> The handler type.
     * @param handler The handler to validate.
     * @return The validated handler.
     */
    @Pure
    public static <H extends InvocationHandler> H validateInvocationHandler( final H handler ) {

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

        return Objects.requireNonNull( handlers, "Result handler list cannot be null." ).stream()
                .map( h -> Objects.requireNonNull( h, "Result handler cannot be null." ) )
                .collect( Collectors.toUnmodifiableList() );

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
        validateParameters( command.parameters() );
        validatePermissions( command.requiredPermissions() );
        validateInvocationHandler( command.invocationHandler() );
        validateResultHandlers( command.resultHandlers() );

        if ( command instanceof MessageCommand c ) {
            validateAliases( c.aliases() );
        }

        return command;

    }

}
