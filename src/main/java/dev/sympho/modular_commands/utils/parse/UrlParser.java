package dev.sympho.modular_commands.utils.parse;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.context.CommandContext;
import dev.sympho.modular_commands.api.command.parameter.parse.InvalidArgumentException;
import dev.sympho.modular_commands.api.command.parameter.parse.ParserFunction;
import reactor.core.publisher.Mono;

/**
 * A parser for URL-based arguments.
 *
 * @param <T> The parsed type.
 * @version 1.0
 * @since 1.0
 */
public interface UrlParser<T extends @NonNull Object> extends ParserFunction<String, T> {

    /**
     * Creates a parser that delegates to other parsers, chosen by the given mapping function
     * based on the URL to parse.
     *
     * @param <T> The parsed argument type.
     * @param parserMapper The function to use to determine which parser to delegate to for
     *                     a given URL.
     * @return The parser.
     */
    static <T extends @NonNull Object> UrlParser<T> choice(
            final Function<URL, @Nullable UrlParser<T>> parserMapper ) {

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
    static <T extends @NonNull Object> UrlParser<T> choice(
            final Collection<? extends UrlParser<T>> parsers ) {

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
    static <T extends @NonNull Object> UrlParser<T> choice( 
            final UrlParser<T>... parsers ) {

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
    static <T extends @NonNull Object> UrlParser<T> choiceHost(
            final Map<String, ? extends UrlParser<T>> parsers ) {

        return choice( UrlParserUtils.toHostMapper( parsers ) );

    }

    /**
     * Parses the given string into a URL.
     *
     * @param raw The string to parse.
     * @return The URL.
     * @throws InvalidArgumentException if the given string is not a valid URL.
     * @apiNote Mainly a wrapper to convert the URL exception into the invalid argument
     *          type.
     */
    @SideEffectFree
    static URL getUrl( final String raw ) throws InvalidArgumentException {

        try {
            return new URL( raw );
        } catch ( final MalformedURLException e ) {
            throw new InvalidArgumentException( "Not a valid URL: " + raw, e );
        }

    }

    /**
     * Checks if the given URL is supported by this parser. If this returns {@code false},
     * calling {@link #parse(CommandContext, URL)} with the same URL <i>will</i> result in
     * an error.
     * 
     * <p>Note that the opposite isn't necessarily true; it is possible for this method to
     * return {@code true} for a given URL while {@link #parse(CommandContext, URL)} results
     * in an error. That just means that the basic structure of the URL was detected as being 
     * <i>compatible</i> with this parser (for example, having a particular host and/or protocol),
     * but was malformed or a variant that the parser doesn't support.
     *
     * @param url The URL to check.
     * @return Whether the URL is compatible with this parser.
     */
    @Pure
    boolean supports( URL url );

    /**
     * Parses the given URL.
     *
     * @param context The execution context.
     * @param url The URL to parse.
     * @return The parsed value. May result in a {@link InvalidArgumentException} if the
     *         URL is invalid.
     * @throws InvalidArgumentException if the URL is invalid.
     */
    @SideEffectFree
    Mono<T> parse( CommandContext context, URL url ) throws InvalidArgumentException;

    @Override
    default Mono<T> parse( final CommandContext context, final String raw ) 
            throws InvalidArgumentException {

        return parse( context, getUrl( raw ) );

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
    default <V extends @NonNull Object> UrlParser<V> then( 
            final ParserFunction<T, V> after ) {

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
     * @param <P1> The type of the first parser.
     * @param <P2> The type of the second parser.
     * @since 1.0
     */
    class PostParser<
                    I extends @NonNull Object, 
                    T extends @NonNull Object, 
                    P1 extends @NonNull UrlParser<I>,
                    P2 extends @NonNull ParserFunction<I, T>
            > extends ParserFunction.PostParser<String, I, T, P1, P2> implements UrlParser<T> {

        /**
         * Creates a new instance.
         *
         * @param parser The first parser to apply.
         * @param postParser The second parser to apply.
         */
        public PostParser( final P1 parser, final P2 postParser ) {

            super( parser, postParser );

        }

        @Override
        public boolean supports( final URL url ) {
            return parser.supports( url );
        }

        @Override
        public Mono<T> parse( final CommandContext context, final URL url ) 
                throws InvalidArgumentException {

            return parser.parse( context, url )
                    .flatMap( i -> postParser.parse( context, i ) );

        }

    }

    /**
     * Parser that supports multiple URL types by delegating to one of a list of parsers. Note
     * that this includes {@link #supports(URL) compatibility checks}.
     *
     * @param <T> The parsed argument type.
     * @param <P> The delegate parser type.
     * @since 1.0
     */
    class Choice<T extends @NonNull Object, P extends UrlParser<T>> 
            implements UrlParser<T> {
            
        /** The parsers to delegate to. */
        private final Function<URL, @Nullable P> parserMapper;

        /**
         * Creates a new instance.
         *
         * @param parserMapper The function to use to determine which parser to delegate to for
         *                     a given URL.
         */
        public Choice( final Function<URL, @Nullable P> parserMapper ) {

            this.parserMapper = parserMapper;

        }

        @Override
        public boolean supports( final URL url ) {

            final var parser = parserMapper.apply( url );
            return parser != null && parser.supports( url );

        }

        /**
         * Retrives the parser to use for the given URL.
         *
         * @param url The URL being parsed.
         * @return The parser to use.
         * @throws InvalidArgumentException if none of the parsers support the given URL.
         */
        @Pure
        protected P getParser( final URL url ) throws InvalidArgumentException {

            final var parser = parserMapper.apply( url );
            if ( parser == null ) {
                throw new InvalidArgumentException( "Unsupported URL: " + url );
            } else {
                return parser;
            }
        }

        @Override
        public Mono<T> parse( final CommandContext context, final URL url ) 
                throws InvalidArgumentException {

            return getParser( url ).parse( context, url );

        }

    }
    
}
