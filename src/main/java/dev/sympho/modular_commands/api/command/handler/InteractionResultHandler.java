package dev.sympho.modular_commands.api.command.handler;

import dev.sympho.modular_commands.api.command.context.InteractionCommandContext;

/**
 * A function that handles the result of an interaction command.
 *
 * @param <C> The type of execution context supported.
 * @version 1.0
 * @since 1.0
 */
public sealed interface InteractionResultHandler<C extends InteractionCommandContext>
        extends ResultHandler<C> permits SlashResultHandler {}
