package dev.sympho.modular_commands.api.command.handler;

import java.util.List;

import dev.sympho.modular_commands.api.command.context.CommandContext;

/**
 * A set of handlers that support text-based (message and slash) invocations.
 *
 * @version 1.0
 * @since 1.0
 */
public interface TextHandlers extends InteractionHandlers, MessageHandlers {

    @Override
    InvocationHandler<CommandContext> invocation();

    @Override
    List<? extends ResultHandler<CommandContext>> result();

    /**
     * A record-based implementation.
     *
     * @param invocation The invocation handler to use.
     * @param result The result handlers to use.
     * @since 1.0
     */
    record Impl(
            InvocationHandler<CommandContext> invocation,
            List<? extends ResultHandler<CommandContext>> result
    ) implements TextHandlers {}
    
}
