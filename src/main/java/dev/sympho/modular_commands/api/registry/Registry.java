package dev.sympho.modular_commands.api.registry;

import java.util.Arrays;
import java.util.Collection;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;
import org.slf4j.LoggerFactory;

import dev.sympho.modular_commands.api.CommandGroup;
import dev.sympho.modular_commands.api.command.Command;
import dev.sympho.modular_commands.api.command.Invocation;
import dev.sympho.modular_commands.api.command.handler.Handlers;

/**
 * A registry that stores commands that may be invoked by users.
 *
 * @version 1.0
 * @since 1.0
 */
public interface Registry {

    /**
     * Retrieves the best command known to this registry that has the given parent and name,
     * and is compatible with the given type.
     *
     * @param <H> The handler type that must be supported.
     * @param invocation The invocation of the command.
     * @param type The handler type.
     * @return The best matching command known to this registry, or {@code null} if none were
     *         found.
     */
    @Pure
    <H extends Handlers> @Nullable Command<? extends H> findCommand( 
            Invocation invocation, Class<H> type );

    /**
     * Retrieves all commands known to this registry that are compatible with the given type.
     *
     * @param <H> The handler type that must be supported.
     * @param type The handler type.
     * @return The compatible commands known to this registry.
     */
    @SideEffectFree
    <H extends Handlers> Collection<Command<? extends H>> getCommands( Class<H> type );

    /**
     * Retrieves the command with the given ID that is registered to this registry.
     *
     * @param id The {@link Command#id() ID} of the command to retrieve.
     * @return The command that was registered with the given ID, or {@code null} if there
     *         is no such command in this registry.
     */
    @Pure
    @Nullable Command<?> getCommand( String id );

    /**
     * Registers a command into this registry.
     *
     * @param command The command to register.
     * @return {@code true} if the command was registered successfully. {@code false} if there
     *         is already a command in this registry with the given ID, or if there is already
     *         a command known to this registry with the given signature and this registry does
     *         not allow overriding. If {@code false}, the registration failed and the call had
     *         no effect.
     * @throws IllegalArgumentException if the given command has an invalid configuration.
     */
    boolean registerCommand( Command<?> command ) throws IllegalArgumentException;

    /**
     * Registers the given commands into this registry.
     *
     * <p>For each of the commands that cannot be added (due to being invalid or using an ID that 
     * is already used by another command), if any, an error will be logged but otherwise it will 
     * be silently ignored. If handling of those cases is necessary, use 
     * {@link #registerCommand(Command)} directly.
     *
     * @param commands The commands to add.
     */
    default void registerCommands( final Collection<? extends Command<?>> commands ) {

        final var logger = LoggerFactory.getLogger( Registry.class );
        commands.forEach( c -> {
            try {
                if ( !registerCommand( c ) ) {
                    logger.error( 
                            "Command {} could not be added due to using an existing ID", c.id() 
                    );
                }
            } catch ( final IllegalArgumentException e ) {
                logger.error( String.format( 
                        "Command %s could not be added due to being invalid", c.id() 
                ), e );
            }
        } );

    }

    /**
     * Registers the given commands into this registry.
     *
     * <p>For each of the commands that cannot be added (due to being invalid or using an ID that 
     * is already used by another command), if any, an error will be logged but otherwise it will 
     * be silently ignored. If handling of those cases is necessary, use 
     * {@link #registerCommand(Command)} directly.
     *
     * @param commands The commands to add.
     */
    default void registerCommands( final CommandGroup commands ) {

        registerCommands( commands.commands() );

    }

    /**
     * Registers the given commands into this registry.
     *
     * <p>For each of the commands that cannot be added (due to being invalid or using an ID that 
     * is already used by another command), if any, an error will be logged but otherwise it will 
     * be silently ignored. If handling of those cases is necessary, use 
     * {@link #registerCommand(Command)} directly.
     *
     * @param commands The commands to add.
     */
    default void registerCommands( final Command<?>... commands ) {

        registerCommands( Arrays.asList( commands ) );

    }

    /**
     * Removes a command from this registry that was registered with the given ID.
     *
     * @param id The {@link Command#id() ID} of the command to remove.
     * @return The removed command, or {@code null} if there was no command in this registry
     *         registered with the given ID.
     */
    @Nullable Command<?> removeCommand( String id );
    
}
