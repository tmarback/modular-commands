package dev.sympho.modular_commands.utils;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableMultiset;

import org.checkerframework.common.value.qual.MatchesRegex;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.Command;
import dev.sympho.modular_commands.api.command.Invocation;
import dev.sympho.modular_commands.api.command.handler.Handlers;
import dev.sympho.modular_commands.api.command.handler.InvocationHandler;
import dev.sympho.modular_commands.api.command.handler.ResultHandler;
import dev.sympho.modular_commands.api.command.parameter.Parameter;
import dev.sympho.modular_commands.api.permission.Group;

/**
 * Utility functions for Commands.
 *
 * @version 1.0
 * @since 1.0
 */
public final class CommandUtils {

    /** Compiled name pattern. */
    private static final Pattern NAME_PATTERN = Pattern.compile( 
            Command.NAME_REGEX );

    /** Compiled display name pattern. */
    private static final Pattern DISPLAY_NAME_PATTERN = Pattern.compile( 
            Command.DISPLAY_NAME_REGEX );

    /** Compiled description pattern. */
    private static final Pattern DESCRIPTION_PATTERN = Pattern.compile( 
            Command.DESCRIPTION_REGEX );

    /** Class should not be instantiated. */
    private CommandUtils() {}

    /**
     * Validates the ID of a command.
     *
     * @param id The ID to validate.
     * @throws NullPointerException if a {@code null} value was found where not allowed. 
     */
    @SideEffectFree
    public static void validateId( final String id ) throws NullPointerException {

        Objects.requireNonNull( id );

    }

    /**
     * Validates the parent of a command.
     *
     * @param parent The parent to validate.
     * @throws IllegalArgumentException if the parent is not valid.
     * @throws NullPointerException if a {@code null} value was found where not allowed. 
     */
    @SideEffectFree
    @SuppressWarnings( "methodref.param" )
    public static void validateParent( final Invocation parent ) 
            throws IllegalArgumentException, NullPointerException {

        Objects.requireNonNull( parent, "Parent cannot be null." );
        parent.chain().forEach( CommandUtils::validateName );

    }

    /**
     * Validates the name of a command.
     *
     * @param name The name to validate.
     * @throws IllegalArgumentException if the name is not valid.
     * @throws NullPointerException if a {@code null} value was found where not allowed. 
     */
    @SideEffectFree
    public static void validateName( 
            final @MatchesRegex( Command.NAME_REGEX ) String name ) 
            throws IllegalArgumentException, NullPointerException {

        Objects.requireNonNull( name, "Name cannot be null." );
        if ( !NAME_PATTERN.matcher( name ).matches() ) {
            throw new IllegalArgumentException( "Invalid name." );
        }
        if ( !name.equals( name.toLowerCase() ) ) {
            throw new IllegalArgumentException( "Name must be all lowercase." );
        }

    }

    /**
     * Validates the display name of a command.
     *
     * @param name The display name to validate.
     * @throws IllegalArgumentException if the display name is not valid.
     * @throws NullPointerException if a {@code null} value was found where not allowed. 
     */
    @SideEffectFree
    public static void validateDisplayName( 
            final @MatchesRegex( Command.DISPLAY_NAME_REGEX ) String name ) 
            throws IllegalArgumentException, NullPointerException {

        Objects.requireNonNull( name, "Display name cannot be null." );
        if ( !DISPLAY_NAME_PATTERN.matcher( name ).matches() ) {
            throw new IllegalArgumentException( "Invalid display name." );
        }

    }

    /**
     * Validates an alias.
     *
     * @param alias The alias to validate.
     * @throws IllegalArgumentException if the alias is not valid.
     * @throws NullPointerException if a {@code null} value was found where not allowed. 
     */
    @SideEffectFree
    public static void validateAlias( 
            final @MatchesRegex( Command.NAME_REGEX ) String alias ) 
            throws IllegalArgumentException, NullPointerException {

        Objects.requireNonNull( alias, "Alias cannot be null." );
        if ( !NAME_PATTERN.matcher( alias ).matches() ) {
            throw new IllegalArgumentException( "Invalid alias." );
        }

    }

    /**
     * Validates the aliases of a command.
     *
     * @param aliases The aliases to validate.
     * @throws IllegalArgumentException if the alias set is not valid.
     * @throws NullPointerException if a {@code null} value was found where not allowed. 
     */
    @SideEffectFree
    public static void validateAliases( 
            final Set<@MatchesRegex( Command.NAME_REGEX ) String> aliases ) 
            throws IllegalArgumentException, NullPointerException {

        Objects.requireNonNull( aliases, "Alias set cannot be null." )
                .forEach( CommandUtils::validateAlias );

    }

    /**
     * Validates the description of a command.
     *
     * @param description The description to validate.
     * @throws IllegalArgumentException if the description is not valid.
     * @throws NullPointerException if a {@code null} value was found where not allowed. 
     */
    @SideEffectFree
    public static void validateDescription( 
            final @MatchesRegex( Command.DESCRIPTION_REGEX ) String description ) 
            throws IllegalArgumentException, NullPointerException {

        Objects.requireNonNull( description, "Description cannot be null." );
        if ( !DESCRIPTION_PATTERN.matcher( description ).matches() ) {
            throw new IllegalArgumentException( "Invalid description." );
        }

    }

    /**
     * Validates the parameters of a command.
     *
     * @param parameters The parameters to validate.
     * @throws IllegalArgumentException if the parameter list is not valid.
     * @throws NullPointerException if a {@code null} value was found where not allowed. 
     */
    @SideEffectFree
    public static void validateParameters( final List<Parameter<?>> parameters ) 
            throws IllegalArgumentException, NullPointerException {

        Objects.requireNonNull( parameters, "Parameter list cannot be null." );
        boolean optional = false;
        for ( final var p : parameters ) {

            Objects.requireNonNull( p, "Parameter specification cannot be null." );
            ParameterUtils.validate( p );
            
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
                throw new IllegalArgumentException( String.format( "Duplicate parameter: %s",
                        entry.getElement() ) );
            }

        }

    }

    /**
     * Validates the required group of a command.
     *
     * @param group The group to validate.
     * @throws NullPointerException if a {@code null} value was found where not allowed. 
     */
    @SideEffectFree
    public static void validateGroup( final Group group ) throws NullPointerException {

        Objects.requireNonNull( group, "Required group cannot be null." );

    }

    /**
     * Validates the invocation handler of a command.
     *
     * @param handler The handler to validate.
     * @throws NullPointerException if a {@code null} value was found where not allowed. 
     */
    @SideEffectFree
    public static void validateInvocationHandler( final InvocationHandler<?> handler ) 
            throws NullPointerException {

        Objects.requireNonNull( handler, "Invocation handler cannot be null." );

    }

    /**
     * Validates the result handlers of a command.
     *
     * @param handlers The result handlers to validate.
     * @throws NullPointerException if a {@code null} value was found where not allowed.
     */
    @SideEffectFree
    public static void validateResultHandlers( final List<? extends ResultHandler<?>> handlers ) 
            throws NullPointerException {

        Objects.requireNonNull( handlers, "Result handler list cannot be null." )
                .forEach( h -> Objects.requireNonNull( h, "Result handler cannot be null." ) );

    }

    /**
     * Validates the handlers of a command.
     *
     * @param handlers The handlers to validate.
     * @throws NullPointerException if a {@code null} value was found where not allowed.
     */
    @SideEffectFree
    public static void validateHandlers( final Handlers handlers ) throws NullPointerException {

        validateInvocationHandler( handlers.invocation() );
        validateResultHandlers( handlers.result() );

    }

    /**
     * Validates a command.
     *
     * @param command The command to validate.
     * @throws IllegalArgumentException if any of the components of the command was invalid.
     * @throws NullPointerException if a {@code null} value was found where not allowed.
     */
    @SideEffectFree
    public static void validateCommand( final Command<?> command )
            throws IllegalArgumentException {

        validateId( command.id() );
        validateName( command.name() );
        validateAliases( command.aliases() );
        validateDisplayName( command.displayName() );
        validateDescription( command.description() );
        validateParameters( command.parameters() );
        validateGroup( command.requiredGroup() );
        validateHandlers( command.handlers() );

    }

}
