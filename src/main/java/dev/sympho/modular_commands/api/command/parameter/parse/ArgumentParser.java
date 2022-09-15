package dev.sympho.modular_commands.api.command.parameter.parse;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.context.CommandContext;
import reactor.core.publisher.Mono;

/**
 * Parses received arguments into their actual value.
 *
 * @param <T> The type of argument that is provided.
 * @param <R> The type of raw argument that is received.
 * @version 1.0
 * @since 1.0
 */
@FunctionalInterface
public interface ArgumentParser<R extends @NonNull Object, T extends @NonNull Object> {

    /**
     * Parses the given raw argument from the user into the corresponding value.
     *
     * @param context The execution context.
     * @param raw The raw argument received from the user.
     * @return A Mono that issues the parsed argument. If the raw value is invalid, it may
     *         fail with a {@link InvalidArgumentException}.
     * @throws InvalidArgumentException if the given argument is not a valid value.
     */
    @SideEffectFree
    Mono<T> parse( CommandContext context, R raw ) throws InvalidArgumentException;
    
}
