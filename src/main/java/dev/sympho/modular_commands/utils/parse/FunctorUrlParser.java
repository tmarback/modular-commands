package dev.sympho.modular_commands.utils.parse;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.context.CommandContext;
import dev.sympho.modular_commands.api.command.parameter.parse.InvalidArgumentException;
import dev.sympho.modular_commands.api.command.parameter.parse.Parsers.Functor;
import reactor.core.publisher.Mono;

/**
 * A parser for URL-based arguments that are independent from the invocation context.
 *
 * @param <T> The parsed type.
 * @version 1.0
 * @since 1.0
 */
public interface FunctorUrlParser<T extends @NonNull Object> extends UrlParser<T> {

    /**
     * Creates a parser that delegates to other parsers, chosen by the given mapping function
     * based on the URL to parse.
     *
     * @param <T> The parsed argument type.
     * @param parserMapper The function to use to determine which parser to delegate to for
     *                     a given URL.
     * @return The parser.
     */
    static <T extends @NonNull Object> FunctorUrlParser<T> choice(
            final Function<URL, @Nullable FunctorUrlParser<T>> parserMapper ) {

        return new Choice<>( parserMapper );

    }

    /**
     * Creates a parser that delegates to the given parsers.
     * 
     * <p>The parser choice is defined as the first parser in the iteration order of the given
     * collection for which {@link #supports(URL)} returns {@code true} for the URL being parsed.
     * This implies that the iteration order matters if, and only if, there are URLs that may be
     * supported by more than one of the parsers in the collection.
     *
     * @param <T> The parsed argument type.
     * @param parsers The parsers to delegate to.
     * @return The parser.
     */
    static <T extends @NonNull Object> FunctorUrlParser<T> choice(
            final Collection<? extends FunctorUrlParser<T>> parsers ) {

        return choice( UrlParserUtils.toMapper( parsers ) );

    }

    /**
     * Creates a parser that delegates to the given parsers.
     * 
     * <p>The parser choice is defined as the first parser in the given order for which 
     * {@link #supports(URL)} returns {@code true} for the URL being parsed.
     * This implies that the iteration order matters if, and only if, there are URLs that 
     * may be supported by more than one of the parsers in the collection.
     *
     * @param <T> The parsed argument type.
     * @param parsers The parsers to delegate to.
     * @return The parser.
     */
    @SafeVarargs
    @SuppressWarnings( "varargs" )
    static <T extends @NonNull Object> FunctorUrlParser<T> choice( 
            final FunctorUrlParser<T>... parsers ) {

        return choice( Arrays.asList( parsers ) );

    }

    /**
     * Creates a parser that delegates to a parser depending on the host of the URL, using
     * the given host-parser mappings.
     *
     * @param <T> The parsed argument type.
     * @param parsers The host-parser mappings to delegate to.
     * @return The parser.
     */
    static <T extends @NonNull Object> FunctorUrlParser<T> choiceHost(
            final Map<String, ? extends FunctorUrlParser<T>> parsers ) {

        return choice( UrlParserUtils.toHostMapper( parsers ) );

    }

    /**
     * Parses the given URL.
     *
     * @param url The URL to parse.
     * @return The parsed value. May result in a {@link InvalidArgumentException} if the
     *         URL is invalid.
     * @throws InvalidArgumentException if the URL is invalid.
     */
    @SideEffectFree
    Mono<T> parse( URL url ) throws InvalidArgumentException;

    @Override
    default Mono<T> parse( final CommandContext context, final URL url ) 
            throws InvalidArgumentException {

        return parse( url );

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
    default <V extends @NonNull Object> UrlParser<V> then( final Functor<T, V> after ) {

        return new PostParser<>( this, after );

    }

    /**
     * A composed parser that first applies this parser to one parser, and then applies the 
     * results to a second parser. If parsing with either parser throws an exception, it is 
     * relayed to the caller of the composed parser.
     * 
     * <p>Note that {@link #supports(URL) compatibility} is defined only by the first parser.
     *
     * @param <I> The intermediary type output by the first parser and consumed by the second.
     * @param <T> The final output type.
     * @param <P> The type of the first parser.
     * @param <P2> The type of the second parser.
     * @since 1.0
     */
    class PostParser<
                    I extends @NonNull Object, 
                    T extends @NonNull Object, 
                    P extends @NonNull FunctorUrlParser<I>,
                    P2 extends @NonNull Functor<I, T>
            > extends UrlParser.PostParser<I, T, P, P2> implements FunctorUrlParser<T> {

        /**
         * Creates a new instance.
         *
         * @param parser The first parser to apply.
         * @param postParser The second parser to apply.
         */
        public PostParser( final P parser, final P2 postParser ) {

            super( parser, postParser );

        }

        @Override
        public Mono<T> parse( final URL url ) throws InvalidArgumentException {

            return parser.parse( url )
                    .flatMap( postParser::parse );

        }

    }

    /**
     * Base for a parser that supports multiple URL types by delegating to one of a list of 
     * parsers. Note that this includes {@link #supports(URL) compatibility checks}.
     *
     * @param <T> The parsed argument type.
     * @param <P> The delegate parser type.
     * @since 1.0
     */
    abstract class ChoiceBase<T extends @NonNull Object, P extends FunctorUrlParser<T>> 
            extends UrlParser.ChoiceBase<T, P> implements FunctorUrlParser<T> {

        /**
         * Creates a new instance.
         *
         * @param parserMapper The function to use to determine which parser to delegate to for
         *                     a given URL.
         */
        protected ChoiceBase( final Function<URL, @Nullable P> parserMapper ) {

            super( parserMapper );

        }

        @Override
        public Mono<T> parse( final URL url ) throws InvalidArgumentException {

            return getParser( url ).parse( url );

        }

    }

    /**
     * Parser that supports multiple URL types by delegating to one of a list of parsers. Note
     * that this includes {@link #supports(URL) compatibility checks}.
     *
     * @param <T> The parsed argument type.
     * @since 1.0
     * @apiNote This is a convenience subtype of {@link FunctorUrlParser.ChoiceBase ChoiceBase} 
     *          with the second parameter already set.
     */
    class Choice<T extends @NonNull Object> extends ChoiceBase<T, FunctorUrlParser<T>> {

        /**
         * Creates a new instance.
         *
         * @param parserMapper The function to use to determine which parser to delegate to for
         *                     a given URL.
         */
        protected Choice( final Function<URL, @Nullable FunctorUrlParser<T>> parserMapper ) {

            super( parserMapper );

        }

    }
    
}
