package dev.sympho.modular_commands.utils;

import java.nio.charset.Charset;
import java.util.function.Function;

import org.checkerframework.dataflow.qual.SideEffectFree;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpStatusClass;
import io.netty.handler.codec.http.HttpUtil;
import reactor.netty.http.client.HttpClientResponse;

/**
 * Utilities for working with HTTP requests.
 *
 * @version 1.0
 * @since 1.0
 */
public final class HttpUtils {

    /** Do not instantiate. */
    private HttpUtils() {}

    /**
     * Retrives the charset used to encode an HTTP response.
     * 
     * <p>Also checks that the request succeeded, throwing a custom exception if it
     * did not.
     *
     * @param <E> The exception type to throw if the request did not succeed.
     * @param response The response metadata.
     * @param errorMapper The function to use to generate an exception with a given error
     *                    message.
     * @return The encoding charset.
     * @throws E if the request did not succeed.
     */
    @SideEffectFree
    public static <E extends RuntimeException> Charset getCharset( 
            final HttpClientResponse response, final Function<String, E> errorMapper ) throws E {

        final var status = response.status();
        if ( status.codeClass() != HttpStatusClass.SUCCESS ) {
            throw errorMapper.apply( "Request failed: " + status );
        }

        final var contentType = response.responseHeaders()
                .get( HttpHeaderNames.CONTENT_TYPE );
        return HttpUtil.getCharset( contentType );

    }

    /**
     * Retrives the charset used to encode an HTTP response.
     * 
     * <p>Also checks that the request succeeded.
     *
     * @param response The response metadata.
     * @return The encoding charset.
     * @throws IllegalStateException if the request did not succeed.
     */
    @SideEffectFree
    public static Charset getCharset( final HttpClientResponse response ) 
            throws IllegalStateException {

        return getCharset( response, IllegalStateException::new );

    }
    
}
