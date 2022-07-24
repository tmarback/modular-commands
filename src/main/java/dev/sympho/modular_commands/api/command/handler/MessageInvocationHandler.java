package dev.sympho.modular_commands.api.command.handler;

import dev.sympho.modular_commands.api.command.context.AnyCommandContext;
import dev.sympho.modular_commands.api.command.context.MessageCommandContext;
import dev.sympho.modular_commands.api.command.result.CommandResult;
import dev.sympho.modular_commands.api.exception.ResultException;
import reactor.core.publisher.Mono;

/**
 * A function that handles the execution of a message-based command.
 *
 * @version 1.0
 * @since 1.0
 */
@FunctionalInterface
public non-sealed interface MessageInvocationHandler extends InvocationHandler {

    /**
     * @see #handle(AnyCommandContext)
     */
    @SuppressWarnings( "checkstyle:javadocmethod" )
    Mono<CommandResult> handle( MessageCommandContext context )
            throws ResultException, Exception;

    @Override
    default Mono<CommandResult> handle( AnyCommandContext context )
            throws ResultException, Exception {
        return handle( ( MessageCommandContext ) context );
    }

}
