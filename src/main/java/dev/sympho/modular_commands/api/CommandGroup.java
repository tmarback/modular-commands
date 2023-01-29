package dev.sympho.modular_commands.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.Command;

/**
 * Encapsulates a group of commands.
 *
 * @version 1.0
 * @since 1.0
 * @apiNote This interface is provided primarily to facilitate the use of dependency-injection
 *          (IoC) frameworks when commands must be created in distinct groups (i.e. cannot be
 *          created individually) and the framework does not have a native way of merging
 *          collections. In those cases, this type can be used to encapsulate these groups,
 *          which can then be combined using {@link #merge(Collection)}.
 */
@FunctionalInterface
public interface CommandGroup {

    /**
     * Retrieves the commands contained in this group.
     *
     * @return The command group.
     * @implSpec As specified by the {@link Pure} annotation, this method should always return
     *           the same instance for a given {@link CommandGroup} instance. Additionally,
     *           the collection should be immutable. 
     */
    @Pure
    Collection<Command<?>> commands();

    /**
     * Creates a command group from the given commands.
     * 
     * <p>Changes made to the given collection are not reflected in the returned instance.
     *
     * @param commands The commands to turn into a command group.
     * @return The equivalent command group.
     */
    @SideEffectFree
    static CommandGroup of( final Collection<? extends Command<?>> commands ) {

        final Collection<Command<?>> copy = List.copyOf( commands );
        return () -> copy;

    }

    /**
     * Creates a command group from the given commands.
     *
     * @param commands The commands to turn into a command group.
     * @return The equivalent command group.
     */
    @SideEffectFree
    static CommandGroup of( final Command<?>... commands ) {

        return of( Arrays.asList( commands ) );

    }

    /**
     * Merges multiple command groups into one.
     *
     * @param groups The groups to merge.
     * @return A group that contains all the commands in the merged groups.
     */
    @SideEffectFree
    //@SuppressWarnings( "methodref.return" )
    static CommandGroup merge( final Collection<CommandGroup> groups ) {

        final var commands = groups.stream()
                .map( CommandGroup::commands )
                .flatMap( Collection::stream )
                .toList();

        return of( commands );

    }
    
}
