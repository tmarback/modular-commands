package dev.sympho.modular_commands.api.command.handler;

import java.util.List;

import dev.sympho.modular_commands.api.command.context.SlashCommandContext;

/**
 * A set of handlers that support slash-based invocations.
 *
 * @version 1.0
 * @since 1.0
 */
public non-sealed interface SlashHandlers extends Handlers {

    @Override
    InvocationHandler<? super SlashCommandContext> invocation();

    @Override
    List<? extends ResultHandler<? super SlashCommandContext>> result();

    /**
     * A record-based implementation.
     *
     * @param invocation The invocation handler to use.
     * @param result The result handlers to use.
     * @since 1.0
     */
    record Impl(
            InvocationHandler<? super SlashCommandContext> invocation,
            List<? extends ResultHandler<? super SlashCommandContext>> result
    ) implements SlashHandlers {}
    
}
