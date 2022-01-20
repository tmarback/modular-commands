package dev.sympho.modular_commands.api.command.handler;

import java.util.function.BiPredicate;

import dev.sympho.modular_commands.api.command.context.CommandContext;
import dev.sympho.modular_commands.api.command.result.CommandResult;

/**
 * A function that handles the result of a command.
 *
 * @param <C> The type of execution context supported.
 * @version 1.0
 * @since 1.0
 */
public sealed interface ResultHandler<C extends CommandContext> 
        extends BiPredicate<C, CommandResult>
        permits MessageResultHandler, InteractionResultHandler {

    /**
     * Handles the result of a command.
     *
     * @param context The context of the command.
     * @param result The result of the command.
     * @return {@code true} if the result was fully handled and no longer needs to be processed.
     *         {@code false} if the result should continue to be processed by subsequent handlers.
     */
    boolean handle( C context, CommandResult result );

    /**
     * {@inheritDoc}
     *
     * @implSpec Delegates to {@link #handle(CommandContext, CommandResult)}.
     */
    @Override
    default boolean test( C context, CommandResult result ) {
        return handle( context, result );
    }
    
}
