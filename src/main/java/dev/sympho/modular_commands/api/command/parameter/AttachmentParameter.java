package dev.sympho.modular_commands.api.command.parameter;

import org.checkerframework.checker.nullness.qual.NonNull;

import dev.sympho.modular_commands.api.command.parameter.parse.AttachmentParser;
import discord4j.core.object.entity.Attachment;

/**
 * Specification for a parameter received as an attachment.
 *
 * @param <T> The type of parameter that is received.
 * @version 1.0
 * @since 1.0
 */
public non-sealed interface AttachmentParameter<T extends @NonNull Object> 
        extends Parameter<T, Attachment>, AttachmentParser<T> {}
