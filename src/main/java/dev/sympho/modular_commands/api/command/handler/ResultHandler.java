package dev.sympho.modular_commands.api.command.handler;

import dev.sympho.modular_commands.api.command.context.AnyCommandContext;
import dev.sympho.modular_commands.api.command.result.CommandResult;

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
     * @return {@code true} if the result was fully handled and no longer needs to be processed.
     *         {@code false} if the result should continue to be processed by subsequent handlers.
     */
    boolean handle( AnyCommandContext context, CommandResult result );
    
}
