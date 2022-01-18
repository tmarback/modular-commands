package dev.sympho.modular_commands.api.command.parameter;

import java.io.Serializable;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.exception.InvalidArgumentException;

// BEGIN LONG LINES
/**
 * Specification for a parameter received for a command.
 *
 * @param <T> The type of parameter that is received.
 * @version 1.0
 * @since 1.0
 * @apiNote If being used for an interaction-compatible command, all
 *          values must be compatible with the
 *          <a href="https://discord.com/developers/docs/interactions/application-commands#application-command-object">
 *          Discord API specification</a>.
 */
// END LONG LINES
public sealed interface Parameter<T extends @NonNull Object> extends Serializable
        permits ChoicesParameter {

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
     * Whether the parameter must be specified to invoke the command.
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

    /**
     * Parses the given raw argument from the user into the corresponding value.
     *
     * @param raw The raw argument received from the user.
     * @return The value specified by the argument.
     * @throws InvalidArgumentException if the given string is not a valid value.
     */
    @SideEffectFree
    T parse( String raw ) throws InvalidArgumentException;
    
}
