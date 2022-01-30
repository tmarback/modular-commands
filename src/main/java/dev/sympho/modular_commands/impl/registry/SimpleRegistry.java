package dev.sympho.modular_commands.impl.registry;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.checkerframework.checker.nullness.qual.Nullable;

import dev.sympho.modular_commands.api.command.Command;
import dev.sympho.modular_commands.api.command.Invocation;
import dev.sympho.modular_commands.api.command.MessageCommand;
import dev.sympho.modular_commands.api.registry.Registry;
import dev.sympho.modular_commands.utils.CommandUtils;

/**
 * A simple, directly-mapped registry implementation that does not support overrides. The
 * (partial) exception to this are aliases, where a command <i>may</i> have an alias that
 * matches the regular invocation of another command (the regular invocation will always
 * have precedence over the alias). It <i>may not</i>, however, have the same alias as
 * another command.
 *
 * @version 1.0
 * @since 1.0
 */
public final class SimpleRegistry implements Registry {

    /** The commands registered to this registry. */
    private final Map<String, Command> commands = new ConcurrentHashMap<>();
    /** The known invocations. */
    private final Map<Invocation, Command> invocations = new ConcurrentHashMap<>();
    /** The known alias invocations. */
    private final Map<Invocation, Command> aliasInvocations = new ConcurrentHashMap<>();

    /**
     * Creates an empty registry.
     */
    public SimpleRegistry() {}

    @Override
    public <C extends Command> @Nullable C findCommand( final Invocation invocation, 
            final Class<? extends C> type ) {

        Command found = invocations.get( invocation );
        if ( found == null ) {
            found = aliasInvocations.get( invocation );
        }
        return type.isInstance( found ) ? type.cast( found ) : null;

    }

    @Override
    public <C extends Command> Collection<C> getCommands( final Class<? extends C> type ) {

        return commands.values().stream()
                .filter( type::isInstance )
                .map( type::cast )
                .collect( Collectors.toUnmodifiableList() );

    }

    @Override
    public @Nullable Command getCommand( final String id ) {

        return commands.get( id );

    }

    @Override
    public synchronized boolean registerCommand( final String id, final Command command ) 
            throws IllegalArgumentException {

        CommandUtils.validateCommand( command );

        final Invocation invocation = command.invocation();
        final Set<Invocation> aliases = command instanceof MessageCommand c 
                ? c.aliasInvocations() : Collections.emptySet();

        if ( invocations.containsKey( invocation ) 
                || !Collections.disjoint( aliases, aliasInvocations.keySet() ) ) {
            return false;
        }

        invocations.put( invocation, command );
        for ( final Invocation alias : aliases ) {
            aliasInvocations.put( alias, command );
        }

        return true;

    }

    @Override
    public synchronized @Nullable Command removeCommand( final String id ) {

        final Command command = commands.remove( id );
        if ( command == null ) {
            return null;
        } else {
            invocations.remove( command.invocation() );
            if ( command instanceof MessageCommand c ) {
                c.aliasInvocations().stream().forEach( aliasInvocations::remove );
            }
            return command;
        }

    }
    
}
