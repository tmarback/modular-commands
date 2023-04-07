package dev.sympho.modular_commands.api.command.parameter.parse;

import java.util.Objects;
import java.util.function.Supplier;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.common.value.qual.IntRange;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.context.CommandContext;
import dev.sympho.modular_commands.api.command.parameter.parse.AttachmentParserStages.Parser;
import dev.sympho.modular_commands.api.command.parameter.parse.AttachmentParserStages.Validator;
import dev.sympho.modular_commands.utils.SizeUtils;
import discord4j.core.object.entity.Attachment;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufMono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClientResponse;

/**
 * Parses received attachment arguments into their actual value.
 *
 * @param <T> The type of argument that is provided.
 * @version 1.0
 * @since 1.0
 */
@FunctionalInterface
public interface AttachmentDataParser<T extends @NonNull Object> 
        extends AttachmentParser<T>, Validator, Parser<T> {

    /** Resources used by all instances. */
    Resources GLOBAL_RESOURCES = new Resources();

    /**
     * @implSpec The default is a no-op.
     */
    @Override
    default void validate( final Attachment attachment ) throws InvalidArgumentException {}

    /**
     * Retrieves the maximum size (in bytes) that the attachment may have.
     *
     * @return The maximum size.
     * @implSpec The default is {@link Integer#MAX_VALUE}.
     */
    @Pure
    default @IntRange( from = 0 ) int maxSize() {
        return Integer.MAX_VALUE;
    }

    @Override
    default Attachment validateRaw( Attachment raw ) throws InvalidArgumentException {
    
        validate( AttachmentParser.super.validateRaw( raw ) );

        final var size = raw.getSize();
        if ( size > maxSize() ) {
            final var max = SizeUtils.format( maxSize() );
            final var got = SizeUtils.format( size );
            final var message = "File must be at most %s (got %s)".formatted( max, got );
            throw new InvalidArgumentException( message );
        }

        return raw;
    
    }

    /**
     * Obtains the URL to download the attachment from.
     *
     * @param value The attachment.
     * @return The URL to download from.
     * @implSpec Defaults to the non-proxy URL.
     */
    @Pure
    default String getUrl( final Attachment value ) {
        return value.getUrl();
    }

    /**
     * Determines the HTTP client to use to fetch the attachment.
     *
     * @param context The execution context.
     * @return The HTTP client to use.
     * @implSpec The default delegates to 
     *           {@link #GLOBAL_RESOURCES}.{@link Resources#getClientGetter()}.
     */
    @SideEffectFree
    default HttpClient getHttpClient( final CommandContext context ) {

        return GLOBAL_RESOURCES.getClientGetter().getClient( context );

    }

    /**
     * @implSpec Fetches the attachment from the {@link #getUrl(Attachment) URL} using a 
     *           client obtained by {@link HttpClient#create()} then delegates to
     *           {@link #parse(CommandContext, HttpClientResponse, ByteBufMono)}.
     */
    @Override
    default Mono<T> parseArgument( final CommandContext context, final Attachment raw ) 
            throws InvalidArgumentException {

        return getHttpClient( context ).get()
                .uri( getUrl( raw ) )
                .responseSingle( ( response, body ) -> parse( context, response, body ) );

    }

    /**
     * Resources used by an instance.
     *
     * @since 1.0
     */
    final class Resources {

        /**
         * The default {@link #setClientGetter(HttpClientRetriever) HTTP client getter}.
         * 
         * <p>It uses the client in the 
         * {@link discord4j.rest.RestResources#getReactorResources() reactor resources}
         * of the invoking {@link CommandContext#getClient() discord client}'s
         * {@link discord4j.core.GatewayDiscordClient#getCoreResources() core resources}
         * (the client used for interacting with Discord's API).
         */
        public static final HttpClientRetriever DEFAULT_CLIENT_GETTER = 
                c -> c.getClient()
                        .getCoreResources()
                        .getReactorResources()
                        .getHttpClient();

        /** The HTTP client getter. */
        private HttpClientRetriever clientGetter = DEFAULT_CLIENT_GETTER;

        /** Creates a new instance. */
        private Resources() {}

        /**
         * Sets the given function to be used to obtain an HTTP client for fetching attachment
         * data unless overriden by the parser.
         *
         * @param getter The getter function to use.
         * @return The getter that was replaced.
         */
        public synchronized HttpClientRetriever setClientGetter( 
                final HttpClientRetriever getter ) {

            final var cur = clientGetter;
            clientGetter = Objects.requireNonNull( getter );
            return cur;
    
        }

        /**
         * Sets the given supplier to be used to obtain an HTTP client for fetching attachment
         * data unless overriden by the parser.
         *
         * @param supplier The supplier to use.
         * @return The getter that was replaced.
         */
        public HttpClientRetriever setClientGetter( 
                final Supplier<? extends HttpClient> supplier ) {

            return setClientGetter( ctx -> supplier.get() );
    
        }

        /**
         * Sets the given HTTP client to be used for fetching attachment
         * data unless overriden by the parser.
         *
         * @param client The client to use.
         * @return The getter that was replaced.
         */
        public HttpClientRetriever setClientGetter( final HttpClient client ) {

            return setClientGetter( ctx -> client );
    
        }

        /**
         * Retrieves the function to be used to obtain an HTTP client for fetching attachment
         * data unless overriden by the parser.
         *
         * @return The getter function.
         */
        public HttpClientRetriever getClientGetter() {

            return clientGetter;

        }

        /**
         * A function that determines the HTTP client that should be used to fetch an attachment's
         * data based on the execution context.
         *
         * @since 1.0
         */
        @FunctionalInterface
        public interface HttpClientRetriever {

            /**
             * Determines the HTTP client to use with the given execution context.
             *
             * @param context The execution context.
             * @return The HTTP client to use.
             */
            HttpClient getClient( CommandContext context );

        }

    }
    
}
