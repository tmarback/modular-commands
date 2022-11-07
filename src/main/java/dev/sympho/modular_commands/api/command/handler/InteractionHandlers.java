package dev.sympho.modular_commands.api.command.handler;

import java.util.List;

import dev.sympho.modular_commands.api.command.context.InteractionCommandContext;

/**
 * A set of handlers that support interaction-based invocations.
 *
 * @version 1.0
 * @since 1.0
 */
public interface InteractionHandlers extends SlashHandlers {

    @Override
    InvocationHandler<? super InteractionCommandContext> invocation();

    @Override
    List<? extends ResultHandler<? super InteractionCommandContext>> result();

    /**
     * A record-based implementation.
     *
     * @param invocation The invocation handler to use.
     * @param result The result handlers to use.
     * @since 1.0
     */
    record Impl(
            InvocationHandler<? super InteractionCommandContext> invocation,
            List<? extends ResultHandler<? super InteractionCommandContext>> result
    ) implements InteractionHandlers {}
    
}
