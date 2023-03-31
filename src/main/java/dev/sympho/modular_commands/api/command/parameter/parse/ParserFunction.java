package dev.sympho.modular_commands.api.command.parameter.parse;

import java.util.Objects;
import java.util.function.BiFunction;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.context.CommandContext;
import dev.sympho.modular_commands.api.command.parameter.Parameter;
import reactor.core.publisher.Mono;

/**
 * A function that parses a value within the context of an execution.
 *
 * @param <T> The type of argument that is provided.
 * @param <R> The type of raw argument that is received.
 * @version 1.0
 * @since 1.0
 */
public interface ParserFunction<R extends @NonNull Object, T extends @NonNull Object>
        extends BiFunction<CommandContext, R, Mono<T>> {

    /**
     * Parses the given raw argument from the user into the corresponding value.
     *
     * @param context The execution context.
     * @param raw The raw argument received from the user.
     * @return A Mono that issues the parsed argument. If the raw value is invalid, it may
     *         fail with a {@link InvalidArgumentException}. May be empty, in which case
     *         the value defers to the {@link Parameter#defaultValue() default} (functionally
     *         the same as if the argument was missing, but without causing an error if the
     *         parameter is {@link Parameter#required() required}).
     * @throws InvalidArgumentException if the given argument is not a valid value.
     */
    @SideEffectFree
    Mono<T> parse( CommandContext context, R raw ) throws InvalidArgumentException;

    /**
     * @implSpec Alias for {@link #parse(CommandContext, Object)}.
     */
    @Override
    default Mono<T> apply( CommandContext context, R raw ) throws InvalidArgumentException {
        return parse( context, raw );
    }

    /**
     * Returns a composed parser that first applies this parser to its input, and then applies
     * the after parser to the result. If parsing with either parser throws an exception, it is 
     * relayed to the caller of the composed parser.
     *
     * @param <V> The type of output of the {@code after} parser, and of the composed parser.
     * @param after The parser to apply after this parser is applied.
     * @return The composed parser.
     */
    @SideEffectFree
    default <V extends @NonNull Object> ParserFunction<R, V> then( 
            final ParserFunction<T, V> after ) {

        return new PostParser<>( this, after );

    }

    /**
     * A composed parser that first applies this parser to one parser, and then applies the 
     * results to a second parser. If parsing with either parser throws an exception, it is 
     * relayed to the caller of the composed parser.
     *
     * @param <R> The original raw type.
     * @param <I> The intermediary type output by the first parser and consumed by the second.
     * @param <T> The final output type.
     * @param <P1> The type of the first parser.
     * @param <P2> The type of the second parser.
     * @since 1.0
     */
    class PostParser<
                    R extends @NonNull Object, 
                    I extends @NonNull Object, 
                    T extends @NonNull Object, 
                    P1 extends @NonNull ParserFunction<R, I>,
                    P2 extends @NonNull ParserFunction<I, T>
            > implements ParserFunction<R, T> {

        /** The first parser to apply. */
        protected final P1 parser;

        /** The second parser to apply. */
        protected final P2 postParser;

        /**
         * Creates a new instance.
         *
         * @param parser The first parser to apply.
         * @param postParser The second parser to apply.
         */
        public PostParser( final P1 parser, final P2 postParser ) {

            this.parser = Objects.requireNonNull( parser );
            this.postParser = Objects.requireNonNull( postParser );

        }

        @Override
        public Mono<T> parse( final CommandContext context, final R raw ) 
                throws InvalidArgumentException {

            return parser.parse( context, raw )
                    .flatMap( i -> postParser.parse( context, i ) );

        }

    }
    
}
