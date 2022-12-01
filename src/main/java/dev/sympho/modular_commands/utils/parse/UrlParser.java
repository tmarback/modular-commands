package dev.sympho.modular_commands.utils.parse;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.checkerframework.checker.nullness.qual.NonNull;
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
     * Creates a parser that delegates to the given parsers.
     *
     * @param <T> The parsed argument type.
     * @param typeName The aggregate type name, for {@link #typeName()}.
     * @param parsers The parsers to delegate to.
     * @return The parser.
     */
    static <T extends @NonNull Object> UrlParser<T> choice( final String typeName,
            final List<? extends UrlParser<T>> parsers ) {

        return new Choice<>( typeName, List.copyOf( parsers ) );

    }

    /**
     * Creates a parser that delegates to the given parsers.
     *
     * @param <T> The parsed argument type.
     * @param typeName The aggregate type name, for {@link #typeName()}.
     * @param parsers The parsers to delegate to.
     * @return The parser.
     */
    @SafeVarargs
    @SuppressWarnings( "varargs" )
    static <T extends @NonNull Object> UrlParser<T> of( final String typeName, 
            final UrlParser<T>... parsers ) {

        return choice( typeName, Arrays.asList( parsers ) );

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
    static URL parseUrl( final String raw ) throws InvalidArgumentException {

        try {
            return new URL( raw );
        } catch ( final MalformedURLException e ) {
            throw new InvalidArgumentException( "Not a valid URL: " + raw, e );
        }

    }

    /**
     * Gets the display name for this type.
     *
     * @return The name.
     * @apiNote Mostly for use in error messages.
     */
    @Pure
    String typeName();

    /**
     * Checks if the given URL is supported by this parser. If this returns {@code false},
     * calling {@link #parse(CommandContext, URL)} with the same URL <i>will</i> result in
     * an error.
     *
     * @param url The URL to check.
     * @return Whether the URL is compatible with this parser.
     */
    @Pure
    boolean supported( URL url );

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

        final URL url = parseUrl( raw );

        if ( !supported( url ) ) {
            throw new InvalidArgumentException( "Not a valid %s URL: %s".formatted( 
                    typeName(), raw ) );
        }

        return parse( context, url );

    }

    /**
     * Parser that supports multiple URL types by delegating to one of a list of parsers.
     * 
     * <p>Note that a parser is chosen as the first in the list whose {@link #supported(URL)}
     * method returns {@code true} for the given URL. This implies that the order of the parsers
     * is relevant if and only if there are URLs that may be parsed by more than one parser in
     * the list.
     *
     * @param <T> The parsed argument type.
     * @param <P> The delegate parser type.
     * @since 1.0
     */
    class Choice<T extends @NonNull Object, P extends UrlParser<T>> 
            implements UrlParser<T> {
            
        /** The aggregate type name, for {@link #typeName()}. */
        private final String typeName;
            
        /** The parsers to delegate to. */
        private final List<P> parsers;

        /**
         * Creates a new instance.
         *
         * @param typeName The aggregate type name, for {@link #typeName()}.
         * @param parsers The parsers to delegate to.
         */
        public Choice( final String typeName, final List<P> parsers ) {

            this.typeName = typeName;
            this.parsers = List.copyOf( parsers );

        }

        @Override
        public boolean supported( final URL url ) {

            return parsers.stream().anyMatch( p -> p.supported( url ) );

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

            return parsers.stream()
                    .filter( p -> p.supported( url ) )
                    .findFirst()
                    .orElseThrow( () -> new InvalidArgumentException( "Unsupported URL: " + url ) );

        }

        @Override
        public Mono<T> parse( final CommandContext context, final URL url ) 
                throws InvalidArgumentException {

            return getParser( url ).parse( context, url );

        }

        @Override
        public Mono<T> parse( final CommandContext context, final String raw ) 
                throws InvalidArgumentException {

            final URL url = UrlParser.parseUrl( raw );
            return getParser( url ).parse( context, url );

        }

        @Override
        public String typeName() {

            return typeName;

        }

    }
    
}
