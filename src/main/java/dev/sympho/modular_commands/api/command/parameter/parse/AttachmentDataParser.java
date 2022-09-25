package dev.sympho.modular_commands.api.command.parameter.parse;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.common.value.qual.IntRange;
import org.checkerframework.dataflow.qual.Pure;

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

    /**
     * @implSpec Fetches the attachment from the normal (non-proxy) URL using a client obtained
     *           by {@link HttpClient#create()} then delegates to
     *           {@link #parse(CommandContext, HttpClientResponse, ByteBufMono)}.
     */
    @Override
    default Mono<T> parse( final CommandContext context, final Attachment raw ) 
            throws InvalidArgumentException {

        validate( raw );

        final var size = raw.getSize();
        if ( size > maxSize() ) {
            final var max = SizeUtils.format( maxSize() );
            final var got = SizeUtils.format( size );
            final var message = "File must be at most %s (got %s)".formatted( max, got );
            throw new InvalidArgumentException( message );
        }

        return HttpClient.create().get()
                .uri( raw.getUrl() )
                .responseSingle( ( response, body ) -> parse( context, response, body ) );

    }
    
}
