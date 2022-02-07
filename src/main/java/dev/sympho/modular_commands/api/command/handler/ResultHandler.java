package dev.sympho.modular_commands.api.command.handler;

import dev.sympho.modular_commands.api.command.context.AnyCommandContext;
import dev.sympho.modular_commands.api.command.result.CommandResult;
import reactor.core.publisher.Mono;

/**
 * A function that handles the result of a command.
 *
 * @version 1.0
 * @since 1.0
 */
public sealed interface ResultHandler
        permits MessageResultHandler, SlashResultHandler {

    /**
     * Handles the result of a command.
     *
     * @param context The context of the command.
     * @param result The result of the command.
     * @return A Mono that issues
     *         {@code true} if the result should continue to be processed by subsequent handlers,
     *         {@code false} if the result was fully handled and no longer needs to be processed.
     *         The Mono may also be empty, in which case handling stops (as if {@code false}).
     * @apiNote Unlike an invocation handler, this handler is <i>not</i> free to throw any 
     *          exceptions. This is due to the fact that no further handling will be done if
     *          a result handler encounters an error, other than logging the exception.
     *          
     */
    Mono<Boolean> handle( AnyCommandContext context, CommandResult result );
    
}
