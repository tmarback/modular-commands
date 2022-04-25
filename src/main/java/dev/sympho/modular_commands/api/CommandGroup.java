package dev.sympho.modular_commands.api;

import java.util.Map;
import java.util.Set;

import org.checkerframework.checker.nullness.qual.KeyFor;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.Command;
import dev.sympho.modular_commands.api.registry.Registry;

/**
 * Encapsulates a group of commands.
 *
 * @version 1.0
 * @since 1.0
 * @apiNote This interface is provided primarily to facilitate the use of dependency-injection
 *          (IoC) frameworks. For example, commands can be easily registered using Spring's
 *          autowiring capabilities by grouping them into {@link CommandGroup} beans then
 *          having Spring collect and provide them to the initializer of a {@link Registry}
 *          bean, which can then register them.
 */
@FunctionalInterface
public interface CommandGroup {

    /**
     * Retrieves the commands contained in this group, keyed by their IDs.
     *
     * @return The ID-keyed command group.
     * @implSpec As specified by the {@link Pure} annotation, this method should always return
     *           the same map instance for a given {@link CommandGroup} instance. Additionally,
     *           the map should be immutable. 
     */
    @Pure
    Map<String, Command> commands();

    /**
     * Retrives the IDs of the commands in this group.
     *
     * @return The command IDs.
     */
    @Pure
    default Set<@KeyFor( "this.commands()" ) String> ids() {
        return commands().keySet();
    }

    /**
     * Creates a command group from the given mappings.
     * 
     * <p>Changes made to the given map are not reflected in the returned instance.
     *
     * @param commands The ID-command mappings to turn into a command group.
     * @return The equivalent command group.
     */
    @SideEffectFree
    static CommandGroup of( final Map<String, Command> commands ) {
        final var copy = Map.copyOf( commands );
        return () -> copy;
    }
    
}
