package dev.sympho.modular_commands.api.command.handler;

import dev.sympho.modular_commands.api.command.context.MessageCommandContext;

/**
 * A function that handles the result of a message-based command.
 *
 * @version 1.0
 * @since 1.0
 */
@FunctionalInterface
public non-sealed interface MessageResultHandler extends ResultHandler<MessageCommandContext> {}
