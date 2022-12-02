package dev.sympho.modular_commands.utils.parse;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

/**
 * Convenience tools for implementing URL parsers.
 *
 * @version 1.0
 * @since 1.0
 */
public final class UrlParserUtils {

    /** The HTTP protocol. */
    public static final String PROTOCOL_HTTP = "http";
    /** The HTTPS protocol. */
    public static final String PROTOCOL_HTTPS = "https";

    /** HTTP-based protocols. */
    public static final List<String> PROTOCOL_HTTP_COMPATIBLE = List.of(
            PROTOCOL_HTTP,
            PROTOCOL_HTTPS
    );

    /** Do not instantiate. */
    private UrlParserUtils() {}

    /* URL part checking */

    /**
     * Checks if the given URL has a HTTP-based protocol (HTTP or HTTPS).
     *
     * @param url The URL to check.
     * @return If the URL has a compatible protocol.
     * @apiNote This method allows HTTPS as well given that it is generally intercompatible
     *          with HTTP and is generally recommended for better security.
     */
    @Pure
    public static boolean isHttp( final URL url ) {

        return PROTOCOL_HTTP_COMPATIBLE.contains( url.getProtocol() );

    }

    /**
     * Checks if the given URL has HTTPS as the protocol.
     *
     * @param url The URL to check.
     * @return If the URL has a compatible protocol.
     */
    @Pure
    public static boolean isHttps( final URL url ) {

        return PROTOCOL_HTTPS.equals( url.getProtocol() );

    }

    /**
     * Checks if the given URL has the given host.
     *
     * @param url The URL to check.
     * @param host The host.
     * @return If the URL has the given host.
     */
    @Pure
    public static boolean isHost( final URL url, final String host ) {

        return host.equals( url.getHost() );

    }

    /**
     * Checks if the given URL has a certain host, which may be specified by any of the
     * given aliases.
     *
     * @param url The URL to check.
     * @param aliases The host aliases.
     * @return If the URL has the given host.
     */
    @Pure
    public static boolean isHost( final URL url, final Collection<String> aliases ) {

        return aliases.contains( url.getHost() );

    }

    /* Choice adapters */

    /**
     * Creates a delegate parser mapper where the parser choice is defined as the first parser 
     * in the iteration order of the given collection for which {@link UrlParser#supported(URL)} 
     * returns {@code true} for the URL being parsed. This implies that the iteration order matters 
     * if, and only if, there are URLs that may be supported by more than one of the parsers in the 
     * collection.
     *
     * @param <T> The parsed argument type.
     * @param <P> The delegate parser type.
     * @param parsers The delegate parsers.
     * @return The mapper.
     */
    @SideEffectFree
    @SuppressWarnings( "argument" ) // It gets weirded out by the null return
    public static <T extends @NonNull Object, P extends UrlParser<T>> 
            Function<URL, @Nullable P> toMapper( final Collection<? extends P> parsers ) {

        return url -> parsers.stream()
                .filter( p -> p.supported( url ) )
                .findFirst()
                .orElse( null );

    }

    /**
     * Creates a delegate parser mapper where the parser choice depends on the host of 
     * the URL, using the given host-parser mappings.
     *
     * @param <T> The parsed argument type.
     * @param <P> The delegate parser type.
     * @param parsers The host-parser mappings.
     * @return The mapper.
     */
    @Pure
    public static <T extends @NonNull Object, P extends UrlParser<T>> 
            Function<URL, @Nullable P> toHostMapper( final Map<String, ? extends P> parsers ) {

        return url -> parsers.get( url.getHost() );

    }
    
}
