package dev.sympho.modular_commands.api.command.parameter.parse;

import org.checkerframework.checker.nullness.qual.NonNull;

import discord4j.core.object.entity.Attachment;

/**
 * Parses received attachment arguments into their actual value.
 *
 * @param <T> The type of argument that is provided.
 * @version 1.0
 * @since 1.0
 */
@FunctionalInterface
public non-sealed interface AttachmentParser<T extends @NonNull Object> 
        extends ArgumentParser<Attachment, T> {}
