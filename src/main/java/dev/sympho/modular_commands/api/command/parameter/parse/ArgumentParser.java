package dev.sympho.modular_commands.api.command.parameter.parse;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.dataflow.qual.Pure;
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
public sealed interface ArgumentParser<R extends @NonNull Object, T extends @NonNull Object> 
        extends ParserFunction<R, T> permits AttachmentParser, InputParser {

    /**
     * Validates the raw value before parsing.
     *
     * @param raw The raw value.
     * @return The raw value.
     * @throws InvalidArgumentException if the value is invalid.
     * @apiNote This method is used to provide built-in validation in text-commands that would 
     *          be done by Discord's servers in slash commands. Client code should place any
     *          custom validation in {@link #parseArgument(CommandContext, Object)}.
     */
    @Pure
    default R validateRaw( final R raw ) throws InvalidArgumentException {
        return raw;
    }

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
    Mono<T> parseArgument( CommandContext context, R raw ) throws InvalidArgumentException;

    /**
     * @apiNote Do not override.
     */
    @Override
    default Mono<T> parse( CommandContext context, R raw ) throws InvalidArgumentException {
        return parseArgument( context, validateRaw( raw ) );
    }

}
