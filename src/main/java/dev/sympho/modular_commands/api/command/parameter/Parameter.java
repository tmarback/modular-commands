package dev.sympho.modular_commands.api.command.parameter;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.parameter.parse.ArgumentParser;
import dev.sympho.modular_commands.utils.builder.ParameterBuilder;

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
 * @param name The name of the parameter.
 * @param description The description of the parameter.
 * @param required Whether the parameter must be provided to invoke the command.
 * @param defaultValue The default value for the parameter.
 * @param parser The parser to use to process received arguments.
 * @version 1.0
 * @since 1.0
 */
// END LONG LINES
public record Parameter<T extends @NonNull Object>(
        String name,
        String description,
        boolean required,
        @Nullable T defaultValue,
        ArgumentParser<?, T> parser
) {

    /**
     * Creates a new builder.
     *
     * @param <T> The argument type.
     * @return The builder.
     */
    @SideEffectFree
    public static <T extends @NonNull Object> ParameterBuilder<T> builder() {
        return new ParameterBuilder<>();
    }

}
