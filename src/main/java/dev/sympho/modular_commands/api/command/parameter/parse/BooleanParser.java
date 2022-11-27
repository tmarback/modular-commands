package dev.sympho.modular_commands.api.command.parameter.parse;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Parses boolean-based input arguments.
 *
 * @param <T> The type of argument that is provided.
 * @version 1.0
 * @since 1.0
 */
public non-sealed interface BooleanParser<T extends @NonNull Object>
        extends InputParser<Boolean, T> {}
