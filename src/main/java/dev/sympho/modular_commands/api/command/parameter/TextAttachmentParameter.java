package dev.sympho.modular_commands.api.command.parameter;

import java.nio.charset.Charset;

import com.google.common.net.MediaType;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.context.CommandContext;
import dev.sympho.modular_commands.api.exception.InvalidArgumentException;
import dev.sympho.modular_commands.utils.HttpUtils;
import discord4j.core.object.entity.Attachment;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufMono;
import reactor.netty.http.client.HttpClientResponse;

/**
 * Specification for a parameter parsed from a text file.
 *
 * @param <T> The type of parameter that is received.
 * @version 1.0
 * @since 1.0
 */
public interface TextAttachmentParameter<T extends @NonNull Object> 
        extends AttachmentParameter<T> {

    /**
     * @implSpec Verifies that the attachment is a text file (any type of MIME text file,
     *           not necessarily .txt).
     */
    @Override
    default void validate( final Attachment attachment ) throws InvalidArgumentException {

        if ( !attachment.getContentType()
                .map( MediaType::parse )
                .map( t -> t.is( MediaType.ANY_TEXT_TYPE ) )
                .orElse( false ) ) {
            throw new InvalidArgumentException( this, "Attachment must be a text file" );
        }

    }

    /**
     * Parses the content of the attachment file.
     *
     * @param context The execution context.
     * @param content The attachment content.
     * @return A Mono that issues the parsed argument. If the content is invalid, it may
     *         fail with a {@link InvalidArgumentException}.
     * @throws InvalidArgumentException if the given content is not valid.
     */
    @SideEffectFree
    Mono<T> parse( CommandContext context, String content ) throws InvalidArgumentException;

    /**
     * @implSpec Parses the attachment file content using {@link #parse(CommandContext, String)}.
     */
    @Override
    default Mono<T> parse( final CommandContext context, final HttpClientResponse response, 
            final ByteBufMono body ) throws InvalidArgumentException {

        final Charset encoding = HttpUtils.getCharset( response, this::invalid );
        return body.asString( encoding ).flatMap( c -> parse( context, c ) );

    }
    
}
