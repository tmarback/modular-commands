package dev.sympho.modular_commands.api.command.handler;

import java.util.List;

import dev.sympho.modular_commands.api.command.context.MessageCommandContext;

/**
 * A set of handlers that support message-based invocations.
 *
 * @version 1.0
 * @since 1.0
 */
public non-sealed interface MessageHandlers extends Handlers {

    @Override
    InvocationHandler<? super MessageCommandContext> invocation();

    @Override
    List<? extends ResultHandler<? super MessageCommandContext>> result();

    /**
     * A record-based implementation.
     *
     * @param invocation The invocation handler to use.
     * @param result The result handlers to use.
     * @since 1.0
     */
    record Impl(
            InvocationHandler<? super MessageCommandContext> invocation,
            List<? extends ResultHandler<? super MessageCommandContext>> result
    ) implements MessageHandlers {}
    
}
