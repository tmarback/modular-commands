package dev.sympho.modular_commands.api.command.handler;

import dev.sympho.modular_commands.api.command.context.AnyCommandContext;
import dev.sympho.modular_commands.api.command.context.CommandContext;
import dev.sympho.modular_commands.api.command.context.InteractionCommandContext;
import dev.sympho.modular_commands.api.command.context.MessageCommandContext;
import dev.sympho.modular_commands.api.command.result.CommandResult;

/**
 * A function that handles the result of any command type.
 *
 * @version 1.0
 * @since 1.0
 */
@FunctionalInterface
public interface AnyResultHandler extends MessageResultHandler, InteractionResultHandler {

    /**
     * @see #handle(AnyCommandContext, CommandResult)
     */
    @SuppressWarnings( "checkstyle:javadocmethod" )
    boolean handle( CommandContext context, CommandResult result );

    @Override
    default boolean handle( MessageCommandContext context, CommandResult result ) {
        return handle( ( CommandContext ) context, result );
    }

    @Override
    default boolean handle( InteractionCommandContext context, CommandResult result ) {
        return handle( ( CommandContext ) context, result );
    }

    @Override
    default boolean handle( AnyCommandContext context, CommandResult result ) {
        return handle( ( CommandContext ) context, result );
    }
    
}
