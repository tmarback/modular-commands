package dev.sympho.modular_commands.api.command.parameter.parse;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Parses received input arguments into their actual value.
 *
 * @param <T> The type of argument that is provided.
 * @version 1.0
 * @since 1.0
 */
@FunctionalInterface
public interface InputParser<T extends @NonNull Object> extends ArgumentParser<String, T> {}
