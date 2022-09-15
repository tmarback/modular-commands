package dev.sympho.modular_commands.api.command.parameter.parse;

import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.context.CommandContext;
import discord4j.core.object.entity.Attachment;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufMono;
import reactor.netty.http.client.HttpClientResponse;

/**
 * Components of an attachment argument parser.
 *
 * @version 1.0
 * @since 1.0
 */
public final class AttachmentParserStages {

    /** Do not instantiate. */
    private AttachmentParserStages() {}

    /**
     * Validates that the attachment is appropriate prior to fetching it.
     *
     * @since 1.0
     * @apiNote Any validation that can be done using only the attachment object (such as
     *          file type, image size, etc) should be done in a validator rather than 
     *          within the {@link Parser parser} to avoid needlessly making network requests.
     */
    @FunctionalInterface
    public interface Validator {

        /**
         * Validates that the attachment is appropriate prior to fetching it.
         *
         * @param attachment The attachment to validate.
         * @throws InvalidArgumentException if the attachment is not valid.
         */
        @SideEffectFree
        void validate( Attachment attachment ) throws InvalidArgumentException;

    }

    /**
     * Parses the response from fetching the attachment file into the corresponding value.
     *
     * @param <T> The result type.
     * @since 1.0
     */
    @FunctionalInterface
    public interface Parser<T> {

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

    }
    
}
