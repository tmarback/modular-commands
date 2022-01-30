package dev.sympho.modular_commands.utils;

import dev.sympho.modular_commands.api.registry.Registry;
import dev.sympho.modular_commands.impl.registry.SimpleRegistry;

/**
 * Provides instances of default registry implementations.
 *
 * @version 1.0
 * @since 1.0
 */
public final class Registries {

    /** Do not instantiate. */
    private Registries() {}

    /**
     * Creates a simple, directly-mapped registry implementation that does not support 
     * overrides. The (partial) exception to this are aliases, where a command <i>may</i> 
     * have an alias that matches the regular invocation of another command (the regular 
     * invocation will always have precedence over the alias). It <i>may not</i>, however, 
     * have the same alias as another command.
     *
     * @return A simple, direct-mapped registry.
     * @apiNote This is the registry with the smallest performance and memory overhead, but
     *          naturally it also has the fewest features.
     */
    public static Registry simpleRegistry() {

        return new SimpleRegistry();

    }
    
}
