package dev.sympho.modular_commands.api.command.handler;

import dev.sympho.modular_commands.api.command.context.AnyCommandContext;
import dev.sympho.modular_commands.api.command.context.SlashCommandContext;
import dev.sympho.modular_commands.api.command.result.CommandResult;

/**
 * A function that handles the result of a slash command.
 *
 * @version 1.0
 * @since 1.0
 */
@FunctionalInterface
public non-sealed interface SlashResultHandler extends ResultHandler {

    /**
     * @see #handle(AnyCommandContext, CommandResult)
     */
    @SuppressWarnings( "checkstyle:javadocmethod" )
    boolean handle( SlashCommandContext context, CommandResult result );

    @Override
    default boolean handle( AnyCommandContext context, CommandResult result ) {
        return handle( ( SlashCommandContext ) context, result );
    }

}
