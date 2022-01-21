package dev.sympho.modular_commands.api.command.handler;

import dev.sympho.modular_commands.api.command.context.AnyCommandContext;
import dev.sympho.modular_commands.api.command.result.CommandResult;
import reactor.core.publisher.Mono;

/**
 * A function that handles the execution of a command.
 *
 * @version 1.0
 * @since 1.0
 */
public sealed interface CommandHandler 
        permits MessageCommandHandler, SlashCommandHandler {

    /**
     * Handles an invocation of the command.
     *
     * @param context The invocation context.
     * @return The invocation result.
     * @throws Exception if an error occurred during handling.
     */
    Mono<CommandResult> handle( AnyCommandContext context ) throws Exception;
    
}
