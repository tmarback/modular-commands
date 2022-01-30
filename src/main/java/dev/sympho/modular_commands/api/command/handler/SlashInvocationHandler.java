package dev.sympho.modular_commands.api.command.handler;

import dev.sympho.modular_commands.api.command.context.AnyCommandContext;
import dev.sympho.modular_commands.api.command.context.SlashCommandContext;
import dev.sympho.modular_commands.api.command.result.CommandResult;
import reactor.core.publisher.Mono;

/**
 * A function that handles the execution of a slash command.
 *
 * @version 1.0
 * @since 1.0
 */
@FunctionalInterface
public non-sealed interface SlashInvocationHandler extends InvocationHandler {

    /**
     * @see #handle(AnyCommandContext)
     */
    @SuppressWarnings( "checkstyle:javadocmethod" )
    Mono<CommandResult> handle( SlashCommandContext context ) throws Exception;

    @Override
    default Mono<CommandResult> handle( AnyCommandContext context ) throws Exception {
        return handle( ( SlashCommandContext ) context );
    }

}
