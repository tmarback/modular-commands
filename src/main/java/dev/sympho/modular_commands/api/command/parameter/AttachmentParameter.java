package dev.sympho.modular_commands.api.command.parameter;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.context.CommandContext;
import dev.sympho.modular_commands.api.exception.InvalidArgumentException;
import discord4j.core.object.entity.Attachment;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufMono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClientResponse;

/**
 * Specification for a parameter received as an attachment.
 *
 * @param <T> The type of parameter that is received.
 * @version 1.0
 * @since 1.0
 */
public non-sealed interface AttachmentParameter<T extends @NonNull Object> 
        extends Parameter<T, Attachment> {

    /**
     * Validates that the attachment is appropriate prior to fetching it.
     *
     * @param attachment The attachment to validate.
     * @throws InvalidArgumentException if the attachment is not valid for the parameter.
     * @apiNote Any validation that can be done using only the attachment object (such as
     *          file type, image size, etc) should be done by overriding this method rather
     *          than within {@link #parse(CommandContext, HttpClientResponse, ByteBufMono)}
     *          to avoid needlessly making network requests.
     * @implSpec The default is a no-op.
     */
    @SideEffectFree
    default void validate( final Attachment attachment ) throws InvalidArgumentException {}

    /**
     * Retrieves the maximum size (in bytes) that the attachment may have.
     *
     * @return The maximum size.
     * @implSpec The default is {@link Integer#MAX_VALUE}.
     */
    @Pure
    default int maxSize() {
        return Integer.MAX_VALUE;
    }

    /**
     * Parses the response from fetching the attachment file into the corresponding value.
     *
     * @param context The execution context.
     * @param response The response metadata.
     * @param body The response body.
     * @return A Mono that issues the parsed argument. If the content is invalid, it may
     *         fail with a {@link InvalidArgumentException}.
     * @throws InvalidArgumentException if the given response is not valid.
     */
    @SideEffectFree
    Mono<T> parse( CommandContext context, HttpClientResponse response, ByteBufMono body ) 
            throws InvalidArgumentException;

    /**
     * @implSpec Fetches the attachment from the normal (non-proxy) URL using a client obtained
     *           by {@link HttpClient#create()} then delegates to
     *           {@link #parse(CommandContext, HttpClientResponse, ByteBufMono)}.
     */
    @Override
    default Mono<T> parse( final CommandContext context, final Attachment raw ) 
            throws InvalidArgumentException {

        validate( raw );

        if ( raw.getSize() > maxSize() ) {
            final var message = "File must be at most %d bytes".formatted( maxSize() );
            throw new InvalidArgumentException( this, message );
        }

        return HttpClient.create().get()
                .uri( raw.getUrl() )
                .responseSingle( ( response, body ) -> parse( context, response, body ) );

    }
    
}
