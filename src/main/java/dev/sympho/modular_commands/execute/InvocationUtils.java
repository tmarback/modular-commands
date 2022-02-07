package dev.sympho.modular_commands.execute;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.Command;
import dev.sympho.modular_commands.api.command.Invocation;
import dev.sympho.modular_commands.api.command.handler.InvocationHandler;
import dev.sympho.modular_commands.api.command.parameter.Parameter;
import dev.sympho.modular_commands.api.exception.InvalidChainException;
import dev.sympho.modular_commands.api.registry.Registry;
import discord4j.rest.util.PermissionSet;

/**
 * Utility functions for handling invocations.
 *
 * @version 1.0
 * @since 1.0
 */
public final class InvocationUtils {

    /** Do not instantiate. */
    private InvocationUtils() {}

    /**
     * Builds an execution chain from a sequence of args by performing lookups on the given
     * registry.
     *
     * @param <C> The command type.
     * @param registry The registry to use for command lookups.
     * @param args The invocation args.
     * @param commandType The command type.
     * @return The execution chain.
     */
    @SideEffectFree
    public static <C extends Command> List<C> makeChain( final Registry registry,
            final List<String> args, final Class<? extends C> commandType ) {

        final List<C> chain = new LinkedList<>();
        Invocation current = Invocation.of();
        for ( final String arg : args ) {

            final var next = current.child( arg );
            final C command = registry.findCommand( next, commandType );
            if ( command == null ) {
                break;
            } else {
                chain.add( command );
                current = current.child( command.name() );
            }

        }
        return new ArrayList<>( chain );

    }

    /**
     * Extracts the command being invoked from an execution chain.
     *
     * @param <C> The command type.
     * @param chain The execution chain.
     * @return The command that was invoked (the last one in the chain).
     */
    @Pure
    public static <C extends Command> C getInvokedCommand( final List<? extends C> chain ) {

        return chain.get( chain.size() - 1 );

    }

    /**
     * Determines the command in the execution chain that should provide the
     * invocation settings.
     *
     * @param chain The command chain.
     * @return The command to take settings from.
     * @see Command#inheritSettings()
     */
    @Pure
    public static Command getSettingsSource( final List<? extends Command> chain ) {

        final var settingIt = chain.listIterator( chain.size() );
        Command settingsSource = settingIt.previous();
        while ( settingIt.hasPrevious() && settingsSource.inheritSettings() ) {
            settingsSource = settingIt.previous();
        }
        return settingsSource;

    }

    /**
     * Determines the total set of build-in permissions required for an execution chain.
     *
     * @param chain The execution chain.
     * @return The required built-in permissions.
     */
    @SideEffectFree
    public static PermissionSet accumulateDiscordPermissions( 
            final List<? extends Command> chain ) {

        final var permIt = chain.listIterator( chain.size() );
        Command permSource = permIt.previous();
        PermissionSet permissions = permSource.requiredDiscordPermissions();
        while ( permIt.hasPrevious() && permSource.requireParentPermissions() ) {
            permSource = permIt.previous();
            permissions = permissions.or( permSource.requiredDiscordPermissions() );
        }
        return permissions;

    }

    /**
     * Determines the sequence of invocation handlers to execute.
     *
     * @param <C> The command type.
     * @param <IH> The handler type.
     * @param chain The invocation chain.
     * @param getter The getter to use to retrieve handlers.
     * @return The handlers to execute.
     * @throws InvalidChainException if the command chain is incompatible.
     * @see Command#invokeParent()
     */
    @SideEffectFree
    public static <C extends Command, IH extends InvocationHandler> List<IH> accumulateHandlers(
                final List<C> chain, final Function<C, IH> getter ) throws InvalidChainException {

        final List<IH> handlers = new LinkedList<>();
        final var it = chain.listIterator( chain.size() );
        C source = it.previous();

        final C target = source;
        final Set<String> satisfiedParameters = source.parameters().stream()
                .filter( p -> p.required() || p.defaultValue() != null )
                .map( Parameter::name )
                .collect( Collectors.toSet() );
        @SuppressWarnings( "rawtypes" )
        final Map<String, Class<? extends Parameter>> parameterTypes = source.parameters().stream()
                .collect( Collectors.toMap( Parameter::name, Parameter::getClass ) );

        while ( it.hasPrevious() && source.invokeParent() ) {

            source = it.previous();
            // Validate command compatibility
            for ( final Parameter<?> p : source.parameters() ) {

                final var receivedType = parameterTypes.get( p.name() );
                // Validate type
                if ( receivedType != null && receivedType != p.getClass() ) {
                    throw new InvalidChainException( source, target, String.format( 
                            "Parameter %s is of type %s in parent but type %s in child", 
                            p.name(), p.getClass().getSimpleName(), receivedType.getSimpleName()
                    ) );
                }
                // Validate presence
                if ( p.required() && !satisfiedParameters.contains( p.name() ) ) {
                    throw new InvalidChainException( source, target, String.format( 
                            "Parameter %s is required in parent but may not be present in child", 
                            p.name()
                    ) );
                }

            }

            handlers.add( 0, getter.apply( source ) );

        }

        return new ArrayList<>( handlers );

    }
    
}
