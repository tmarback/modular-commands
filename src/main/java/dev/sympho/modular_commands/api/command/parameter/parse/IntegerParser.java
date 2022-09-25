package dev.sympho.modular_commands.api.command.parameter.parse;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Parses integer-based input arguments.
 *
 * @param <T> The type of argument that is provided.
 * @version 1.0
 * @since 1.0
 */
@FunctionalInterface
public non-sealed interface IntegerParser<T extends @NonNull Object> 
        extends NumberParser<Long, T> {}
