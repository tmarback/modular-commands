package dev.sympho.modular_commands.api.command.parameter;

import org.checkerframework.checker.nullness.qual.NonNull;

import dev.sympho.modular_commands.api.command.parameter.parse.TextFileParser;

/**
 * Specification for a parameter parsed from a text file.
 *
 * @param <T> The type of parameter that is received.
 * @version 1.0
 * @since 1.0
 */
public interface TextAttachmentParameter<T extends @NonNull Object> 
        extends AttachmentParameter<T>, TextFileParser<T> {}
