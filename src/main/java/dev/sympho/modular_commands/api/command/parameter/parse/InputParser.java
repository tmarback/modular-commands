package dev.sympho.modular_commands.api.command.parameter.parse;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Parses received input arguments.
 *
 * @param <P> The primitive type to be parsed.
 * @param <T> The type of argument that is provided.
 * @version 1.0
 * @since 1.0
 */
public sealed interface InputParser<P extends @NonNull Object, T extends @NonNull Object> 
        extends ArgumentParser<P, T> 
        permits BooleanParser, ChoicesParser, SnowflakeParser, EntityArgumentParser {}
