package dev.sympho.modular_commands.api.command.handler;

import dev.sympho.modular_commands.api.command.context.SlashCommandContext;

/**
 * A function that handles the result of a slash command.
 *
 * @version 1.0
 * @since 1.0
 */
@FunctionalInterface
public non-sealed interface SlashResultHandler
        extends InteractionResultHandler<SlashCommandContext> {}
