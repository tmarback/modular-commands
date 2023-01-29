package dev.sympho.modular_commands.api.command.handler;

import java.util.function.BiFunction;

import org.checkerframework.checker.nullness.qual.NonNull;

import dev.sympho.modular_commands.api.command.context.CommandContext;
import dev.sympho.modular_commands.api.command.result.CommandResult;
import reactor.core.publisher.Mono;

/**
 * A function that handles the result of a command.
 *
 * @param <C> The context type.
 * @version 1.0
 * @since 1.0
 */
@FunctionalInterface
public interface ResultHandler<C extends @NonNull CommandContext> 
        extends BiFunction<C, CommandResult, Mono<Boolean>> {

    /**
     * Handles the result of a command.
     *
     * @param context The context of the command.
     * @param result The result of the command.
     * @return A Mono that issues
     *         {@code true} if the result was fully handled and no longer needs to be processed,
     *         {@code false} if the result should continue to be processed by subsequent handlers.
     *         The Mono may also be empty, in which case it is equivalent to {@code false}.
     * @apiNote Unlike an invocation handler, this handler is <i>not</i> free to throw any 
     *          exceptions. This is due to the fact that no further handling will be done if
     *          a result handler encounters an error, other than logging the exception.
     *          
     */
    Mono<Boolean> handle( C context, CommandResult result );

    /**
     * @implSpec Delegates to {@link #handle(CommandContext, CommandResult)}. Do not override.
     */
    @Override
    default Mono<Boolean> apply( final C context, final CommandResult result ) {
        return handle( context, result );
    }
    
}
