package dev.sympho.modular_commands.utils.parse;

import org.checkerframework.checker.nullness.qual.NonNull;

import dev.sympho.modular_commands.api.command.context.CommandContext;
import dev.sympho.modular_commands.api.command.parameter.parse.ParserFunction;
import reactor.core.publisher.Mono;

/**
 * Adapter that converts an existing parser into a string parser, by first pre-parsing
 * the string into another raw type.
 *
 * @param <R> The raw type of the original parser.
 * @param <T> The parsed argument type.
 * @param rawParser The parser to use to pre-parse into the raw type.
 * @param valueParser The parser to use to parse the raw type.
 * @version 1.0
 * @since 1.0
 */
public record StringAdapter<R extends @NonNull Object, T extends @NonNull Object>(
        ParserFunction<String, R> rawParser,
        ParserFunction<R, T> valueParser
) implements ParserFunction<String, T> {

    @Override
    public Mono<T> parse( final CommandContext context, final String raw ) {

        return rawParser.parse( context, raw )
                .flatMap( value -> valueParser.parse( context, value ) );

    }
    
}
