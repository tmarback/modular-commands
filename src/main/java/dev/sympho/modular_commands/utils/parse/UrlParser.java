package dev.sympho.modular_commands.utils.parse;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
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
     * A parser that supports multiple URL types by delegating to one of a list of parsers.
     * 
     * <p>Note that a parser is chosen as the first in the list whose {@link #supported(URL)}
     * method returns {@code true} for the given URL. This implies that the order of the parsers
     * is relevant if and only if there are URLs that may be parsed by more than one parser in
     * the list.
     *
     * @param <T> The parsed argument type.
     * @param typeName The aggregate type name, for {@link #typeName()}.
     * @param parsers The parsers to delegate to.
     * @since 1.0
     */
    record Choice<T extends @NonNull Object>(
            String typeName,
            List<UrlParser<T>> parsers
    ) implements UrlParser<T> {

        /**
         * Creates a parser that delegates to the given parsers.
         *
         * @param <T> The parsed argument type.
         * @param typeName The aggregate type name, for {@link #typeName()}.
         * @param parsers The parsers to delegate to.
         * @return The parser.
         */
        public static <T extends @NonNull Object> Choice<T> of( final String typeName,
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
        public static <T extends @NonNull Object> Choice<T> of( final String typeName, 
                final UrlParser<T>... parsers ) {

            return of( typeName, Arrays.asList( parsers ) );

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
        private UrlParser<T> getParser( final URL url ) throws InvalidArgumentException {

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

            final URL url = parseUrl( raw );
            return getParser( url ).parse( context, url );

        }

    }

    /**
     * A parser that only accepts URLs with HTTP(S) protocol.
     *
     * @param <T> The parsed argument type.
     * @since 1.0
     */
    interface Http<T extends @NonNull Object> extends UrlParser<T> {

        /** The accepted protocols. */
        List<String> PROTOCOLS = List.of(
                "http",
                Https.PROTOCOL
        );

        /**
         * @apiNote Implementations probably want to add additional validation to this:
         *     <pre>
         *     {@code
         *     @Override
         *     public boolean supported(final URL url) {
         *       return super.supported() 
         *         && somePath.equals( url.getPath() )
         *         && ...
         *     }
         *     }
         *     </pre>
         */
        @Override
        default boolean supported( final URL url ) {

            return PROTOCOLS.contains( url.getProtocol() );

        }

    }

    /**
     * A parser that only accepts URLs with HTTPS protocol.
     *
     * @param <T> The parsed argument type.
     * @since 1.0
     */
    interface Https<T extends @NonNull Object> extends UrlParser<T> {

        /** The accepted protocol. */
        String PROTOCOL = "https";

        /**
         * @apiNote Implementations probably want to add additional validation to this:
         *     <pre>
         *     {@code
         *     @Override
         *     public boolean supported(final URL url) {
         *       return super.supported() 
         *         && somePath.equals( url.getPath() )
         *         && ...
         *     }
         *     }
         *     </pre>
         */
        @Override
        default boolean supported( final URL url ) {

            return PROTOCOL.equals( url.getProtocol() );

        }

    }

    /**
     * A parser that accepts URLs from a single host.
     *
     * @param <T> The parsed argument type.
     * @since 1.0
     */
    interface Host<T extends @NonNull Object> extends UrlParser<T> {

        /**
         * Retrieves the supported host.
         *
         * @return The host.
         */
        @Pure
        String host();

        /**
         * @apiNote Implementations probably want to add additional validation to this:
         *     <pre>
         *     {@code
         *     @Override
         *     public boolean supported(final URL url) {
         *       return super.supported() 
         *         && somePath.equals( url.getPath() )
         *         && ...
         *     }
         *     }
         *     </pre>
         */
        @Override
        default boolean supported( final URL url ) {

            return host().equals( url.getHost() );

        }

    }

    /**
     * A parser that accepts URLs from a set of hosts that are aliases of each other,
     * which means that parsing is independent from which of them is used.
     *
     * @param <T> The parsed argument type.
     * @since 1.0
     */
    interface HostAlias<T extends @NonNull Object> extends UrlParser<T> {

        /**
         * Retrieves the supported hosts.
         *
         * @return The hosts.
         */
        @Pure
        Set<String> hosts();

        /**
         * @apiNote Implementations probably want to add additional validation to this:
         *     <pre>
         *     {@code
         *     @Override
         *     public boolean supported(final URL url) {
         *       return super.supported() 
         *         && somePath.equals( url.getPath() )
         *         && ...
         *     }
         *     }
         *     </pre>
         */
        @Override
        default boolean supported( final URL url ) {

            return hosts().contains( url.getHost() );

        }

    }

    /**
     * A parser that accepts URLs from a set of hosts that are <i>not</i> aliases of each other,
     * which means that a host-dependent pre-parsing step is applied.
     *
     * @param <T> The parsed argument type.
     * @param <I> The intermediate type generated by the pre-parsing step.
     * @since 1.0
     */
    interface HostBased<T extends @NonNull Object, I extends @NonNull Object> extends UrlParser<T> {

        /**
         * Retrieves the pre-parser for the given host.
         *
         * @param host The host.
         * @return The pre-parser for the given host, or {@code null} if the host is
         *         not supported.
         */
        @Pure
        @Nullable Function<URL, I> preParser( String host );

        /**
         * @apiNote Implementations probably want to add additional validation to this:
         *     <pre>
         *     {@code
         *     @Override
         *     public boolean supported(final URL url) {
         *       return super.supported() 
         *         && somePath.equals( url.getPath() )
         *         && ...
         *     }
         *     }
         *     </pre>
         */
        @Override
        default boolean supported( final URL url ) {

            return preParser( url.getHost() ) != null;

        }

        /**
         * Parses the value generated by the pre-parsing step.
         *
         * @param context The invocation context.
         * @param value The pre-parsed value.
         * @return The parsed value.
         */
        @SideEffectFree
        Mono<T> parseValue( CommandContext context, I value );

        @Override
        default Mono<T> parse( final CommandContext context, final URL url ) 
                throws InvalidArgumentException {

            final var preParser = preParser( url.getHost() );
            if ( preParser == null ) {
                throw new InvalidArgumentException( "Unsupported host" );
            }
            
            final var value = preParser.apply( url );
            return parseValue( context, value );

        }

    }
    
}
