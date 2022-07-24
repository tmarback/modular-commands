package dev.sympho.modular_commands.api.command.handler;

import dev.sympho.modular_commands.api.command.context.AnyCommandContext;
import dev.sympho.modular_commands.api.command.context.CommandContext;
import dev.sympho.modular_commands.api.command.context.InteractionCommandContext;
import dev.sympho.modular_commands.api.command.context.MessageCommandContext;
import dev.sympho.modular_commands.api.command.result.CommandResult;
import dev.sympho.modular_commands.api.exception.ResultException;
import reactor.core.publisher.Mono;

/**
 * A function that handles the execution of any command type.
 *
 * @version 1.0
 * @since 1.0
 */
@FunctionalInterface
public interface AnyInvocationHandler 
        extends MessageInvocationHandler, InteractionInvocationHandler {

    /**
     * @see #handle(AnyCommandContext)
     */
    @SuppressWarnings( "checkstyle:javadocmethod" )
    Mono<CommandResult> handle( CommandContext context ) throws ResultException, Exception;

    @Override
    default Mono<CommandResult> handle( MessageCommandContext context ) 
            throws ResultException, Exception {
        return handle( ( CommandContext ) context );
    }

    @Override
    default Mono<CommandResult> handle( InteractionCommandContext context )
            throws ResultException, Exception {
        return handle( ( CommandContext ) context );
    }

    @Override
    default Mono<CommandResult> handle( AnyCommandContext context ) 
            throws ResultException, Exception {
        return handle( ( CommandContext ) context );
    }

}
