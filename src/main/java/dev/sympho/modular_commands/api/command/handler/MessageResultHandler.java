package dev.sympho.modular_commands.api.command.handler;

import dev.sympho.modular_commands.api.command.context.AnyCommandContext;
import dev.sympho.modular_commands.api.command.context.MessageCommandContext;
import dev.sympho.modular_commands.api.command.result.CommandResult;
import reactor.core.publisher.Mono;

/**
 * A function that handles the result of a message-based command.
 *
 * @version 1.0
 * @since 1.0
 */
@FunctionalInterface
public non-sealed interface MessageResultHandler extends ResultHandler {

    /**
     * @see #handle(AnyCommandContext, CommandResult)
     */
    @SuppressWarnings( "checkstyle:javadocmethod" )
    Mono<Boolean> handle( MessageCommandContext context, CommandResult result );

    @Override
    default Mono<Boolean> handle( AnyCommandContext context, CommandResult result ) {
        return handle( ( MessageCommandContext ) context, result );
    }

}
