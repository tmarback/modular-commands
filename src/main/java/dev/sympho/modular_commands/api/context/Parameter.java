package dev.sympho.modular_commands.api.context;

import java.io.Serializable;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import dev.sympho.modular_commands.api.exception.InvalidArgumentException;

/**
 * Specification for a parameter received for a command.
 *
 * @param <T> The type of parameter that is received.
 * @version 1.0
 * @since 1.0
 */
public sealed interface Parameter<T extends @NonNull Object> extends Serializable
        permits ChoicesParameter {

    // BEGIN LONG LINES
    /**
     * The name of the parameter.
     *
     * @return The name.
     * @apiNote The name must be valid as per the 
     *          <a href="https://discord.com/developers/docs/interactions/application-commands#application-command-object-application-command-naming">
     *          Discord API specification</a>.
     */
    // END LONG LINES
    String name();

    /**
     * The description of the parameter.
     *
     * @return The description.
     * @apiNote The description must have between 1 and 100 characters.
     */
    String description();

    /**
     * Whether the parameter must be specified to invoke the command.
     *
     * @return {@code true} if the parameter is required, {@code false} otherwise.
     */
    boolean required();

    /**
     * The default value for the parameter.
     *
     * @return The default value, or {@code null} if no default.
     * @apiNote If the parameter is {@link #required() required}, then this
     *          value has no effect.
     */
    @Nullable T defaultValue();

    /**
     * Parses the given raw argument from the user into the corresponding value.
     *
     * @param raw The raw argument received from the user.
     * @return The value specified by the argument.
     * @throws InvalidArgumentException if the given string is not a valid value.
     */
    T parse( String raw ) throws InvalidArgumentException;
    
}
