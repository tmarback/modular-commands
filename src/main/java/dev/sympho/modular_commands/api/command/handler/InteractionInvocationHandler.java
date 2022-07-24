package dev.sympho.modular_commands.api.command.handler;

import dev.sympho.modular_commands.api.command.context.AnyCommandContext;
import dev.sympho.modular_commands.api.command.context.InteractionCommandContext;
import dev.sympho.modular_commands.api.command.context.SlashCommandContext;
import dev.sympho.modular_commands.api.command.result.CommandResult;
import dev.sympho.modular_commands.api.exception.ResultException;
import reactor.core.publisher.Mono;

/**
 * A function that handles the execution of an interaction command.
 *
 * @version 1.0
 * @since 1.0
 */
@FunctionalInterface
public interface InteractionInvocationHandler extends SlashInvocationHandler {

    /**
     * @see #handle(AnyCommandContext)
     */
    @SuppressWarnings( "checkstyle:javadocmethod" )
    Mono<CommandResult> handle( InteractionCommandContext context )
            throws ResultException, Exception;

    @Override
    default Mono<CommandResult> handle( SlashCommandContext context ) 
            throws ResultException, Exception {
        return handle( ( InteractionCommandContext ) context );
    }

}
