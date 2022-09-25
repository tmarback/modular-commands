package dev.sympho.modular_commands.api.command.parameter.parse;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Parses received arguments into their actual value.
 *
 * @param <T> The type of argument that is provided.
 * @param <R> The type of raw argument that is received.
 * @version 1.0
 * @since 1.0
 */
public sealed interface ArgumentParser<R extends @NonNull Object, T extends @NonNull Object> 
        extends ParserFunction<R, T> permits AttachmentParser, InputParser {}
