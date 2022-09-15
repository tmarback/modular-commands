package dev.sympho.modular_commands.api.command.parameter;

import java.io.Serializable;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.parameter.parse.ArgumentParser;

// BEGIN LONG LINES
/**
 * Specification for a parameter received for a command.
 * 
 * <p>Irrespective of whether the command it is used with is compatible with interactions
 * or not, all values must be compatible with the
 * <a href="https://discord.com/developers/docs/interactions/application-commands#application-command-object">
 * Discord API specification</a> for command parameters.
 *
 * @param <T> The type of argument that is provided.
 * @param <R> The type of raw argument that is received.
 * @version 1.0
 * @since 1.0
 */
// END LONG LINES
public sealed interface Parameter<T extends @NonNull Object, R extends @NonNull Object>
        extends Serializable, ArgumentParser<R, T> permits InputParameter, AttachmentParameter {

    /**
     * The name of the parameter.
     *
     * @return The name.
     */
    @Pure
    String name();

    /**
     * The description of the parameter.
     *
     * @return The description.
     * @apiNote The description must have between 1 and 100 characters.
     */
    @Pure
    String description();

    /**
     * Whether the parameter must be provided to invoke the command.
     *
     * @return {@code true} if the parameter is required, {@code false} otherwise.
     */
    @Pure
    boolean required();

    /**
     * The default value for the parameter.
     *
     * @return The default value, or {@code null} if no default.
     * @apiNote If the parameter is {@link #required() required}, then this
     *          value has no effect.
     */
    @SideEffectFree
    @Nullable T defaultValue();
    
}
