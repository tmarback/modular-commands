package dev.sympho.modular_commands.api.command.handler;

import dev.sympho.modular_commands.api.command.context.InteractionCommandContext;
import dev.sympho.modular_commands.api.command.context.SlashCommandContext;
import dev.sympho.modular_commands.api.command.result.CommandResult;
import reactor.core.publisher.Mono;

/**
 * A function that handles the result of an interaction command.
 *
 * @version 1.0
 * @since 1.0
 */
@FunctionalInterface
public interface InteractionResultHandler extends SlashResultHandler {

    /**
     * @see #handle(AnyCommandContext, CommandResult)
     */
    @SuppressWarnings( "checkstyle:javadocmethod" )
    Mono<Boolean> handle( InteractionCommandContext context, CommandResult result );

    @Override
    default Mono<Boolean> handle( SlashCommandContext context, CommandResult result ) {
        return handle( ( InteractionCommandContext ) context, result );
    }

}
