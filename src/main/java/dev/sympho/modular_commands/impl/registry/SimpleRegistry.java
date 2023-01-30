package dev.sympho.modular_commands.impl.registry;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.sympho.modular_commands.api.command.Command;
import dev.sympho.modular_commands.api.command.Invocation;
import dev.sympho.modular_commands.api.command.handler.Handlers;
import dev.sympho.modular_commands.api.command.handler.MessageHandlers;
import dev.sympho.modular_commands.api.registry.Registry;
import dev.sympho.modular_commands.execute.InvocationUtils;
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

    /** The logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger( SimpleRegistry.class );

    /** The commands registered to this registry. */
    private final Map<String, Command<?>> commands = new ConcurrentHashMap<>();
    /** The known invocations. */
    private final Map<Invocation, Command<?>> invocations = new ConcurrentHashMap<>();
    /** The known alias invocations. */
    private final Map<Invocation, Command<?>> aliasInvocations = new ConcurrentHashMap<>();

    /**
     * Creates an empty registry.
     */
    public SimpleRegistry() {}

    @Override
    public <H extends Handlers> @Nullable Command<? extends H> findCommand( 
            final Invocation invocation, final Class<H> type ) {

        Command<?> found = invocations.get( invocation );
        if ( found == null ) {
            found = aliasInvocations.get( invocation );
        } 
        return found == null ? null : InvocationUtils.checkType( found, type );

    }

    @Override
    @SuppressWarnings( "return" ) // https://github.com/typetools/checker-framework/issues/5237
    public <H extends Handlers> Collection<Command<? extends H>> getCommands( 
            final Class<H> type ) {

        return commands.values().stream()
                .map( c -> ( Command<? extends H> ) InvocationUtils.checkType( c, type ) )
                .filter( Objects::nonNull )
                .collect( Collectors.toUnmodifiableList() );

    }

    @Override
    public @Nullable Command<?> getCommand( final String id ) {

        return commands.get( id );

    }

    @Override
    public synchronized boolean registerCommand( final Command<?> command )
            throws IllegalArgumentException {

        CommandUtils.validateCommand( command );

        final String id = command.id();

        LOGGER.info( "Registering command {}", id );

        final Invocation invocation = command.invocation();
        final Set<Invocation> aliases = command.handlers() instanceof MessageHandlers
                ? command.aliasInvocations() : Collections.emptySet();

        if ( invocations.containsKey( invocation ) 
                || !Collections.disjoint( aliases, aliasInvocations.keySet() ) ) {
            return false;
        }

        commands.put( id, command );
        invocations.put( invocation, command );
        for ( final Invocation alias : aliases ) {
            aliasInvocations.put( alias, command );
        }

        return true;

    }

    @Override
    public synchronized @Nullable Command<?> removeCommand( final String id ) {

        final Command<?> command = commands.remove( id );
        if ( command == null ) {
            return null;
        } else {
            invocations.remove( command.invocation() );
            if ( command.handlers() instanceof MessageHandlers ) {
                command.aliasInvocations().stream().forEach( aliasInvocations::remove );
            }
            return command;
        }

    }
    
}
