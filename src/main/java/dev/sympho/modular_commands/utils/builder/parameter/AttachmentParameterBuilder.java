package dev.sympho.modular_commands.utils.builder.parameter;

import java.util.Objects;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.common.value.qual.IntRange;
import org.checkerframework.dataflow.qual.Deterministic;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.context.CommandContext;
import dev.sympho.modular_commands.api.command.parameter.AttachmentParameter;
import dev.sympho.modular_commands.api.command.parameter.parse.AttachmentParserStages.Parser;
import dev.sympho.modular_commands.api.command.parameter.parse.AttachmentParserStages.Validator;
import dev.sympho.modular_commands.api.command.parameter.parse.InvalidArgumentException;
import discord4j.core.object.entity.Attachment;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufMono;
import reactor.netty.http.client.HttpClientResponse;

/**
 * Builder for an attachment parameter for arbitrary object types.
 *
 * @param <T> The type of value received by the parameter.
 * @version 1.0
 * @since 1.0
 */
@SuppressWarnings( "checkstyle:hiddenfield" )
public final class AttachmentParameterBuilder<T extends @NonNull Object> extends 
        AttachmentParameterBuilderBase<T, AttachmentParameter<T>, AttachmentParameterBuilder<T>> {

    /** Default no-op validator. */
    public static final Validator DEFAULT_VALIDATOR = attachment -> {};

    /** The validator to use. */
    protected Validator validator;

    /** The parser to use. */
    protected @MonotonicNonNull Parser<T> parser;

    /**
     * Constructs a new builder with default values.
     */
    @Pure
    public AttachmentParameterBuilder() {

        this.validator = DEFAULT_VALIDATOR;
        this.parser = null;

    }

    /**
     * Constructs a new builder that is a copy of the given builder.
     *
     * @param base The builder to copy.
     */
    @SideEffectFree
    public AttachmentParameterBuilder( 
            final AttachmentParameterBuilder<T> base ) {

        super( base );

        this.validator = base.validator;
        this.parser = base.parser;

    }

    /**
     * Constructs a new builder that is initialized to make a copy of 
     * the given parameter.
     *
     * @param base The parameter to copy.
     */
    @SideEffectFree
    protected AttachmentParameterBuilder( final Parameter<T> base ) {

        super( base );

        this.validator = base.validator;
        this.parser = base.parser;

    }

    /**
     * Sets the validator to use to verify the attachment before downloading.
     *
     * @param validator The validator to use.
     * @return This builder.
     * @see Validator
     */
    @Deterministic
    public AttachmentParameterBuilder<T> withValidator( final @Nullable Validator validator ) {

        this.validator = Objects.requireNonNullElse( validator, DEFAULT_VALIDATOR );
        return this;

    }

    /**
     * Sets the parser to use to process the downloaded attachment.
     *
     * @param parser The parser to use.
     * @return This builder.
     * @see Parser
     */
    @Deterministic
    public AttachmentParameterBuilder<T> withParser( final Parser<T> parser ) {

        this.parser = Objects.requireNonNull( parser );
        return this;

    }

    @Override
    public AttachmentParameter<T> build() throws IllegalStateException {

        if ( parser == null ) {
            throw new IllegalStateException( "Parser must be set before building." );
        }

        try {
            return new Parameter<>( 
                    buildName(),
                    buildDescription(),
                    this.required, this.defaultValue,
                    this.maxSize, validator, parser );
        } catch ( final IllegalArgumentException e ) {
            throw new IllegalStateException( "Invalid parameter configuration.", e );
        }

    }

    /**
     * An arbitrary attachment parameter.
     *
     * @param <T> The type of value received by the parameter.
     * @param name The name of the parameter.
     * @param description The description of the parameter.
     * @param required Whether the parameter must be specified to invoke the command.
     * @param defaultValue The default value for the parameter.
     * @param maxSize The maximum size of text file accepted (in bytes).
     * @param validator The validator to use.
     * @param parser The parser to use.
     * @version 1.0
     * @since 1.0
     */
    public record Parameter<T extends @NonNull Object>(
            String name,
            String description,
            boolean required,
            @Nullable T defaultValue,
            @IntRange( from = 0 ) int maxSize,
            Validator validator,
            Parser<T> parser
    ) implements AttachmentParameter<T> {

        @Override
        public void validate( final Attachment attachment ) {

            validator.validate( attachment );

        }

        @Override
        public Mono<T> parse( final CommandContext context, final HttpClientResponse response,
                final ByteBufMono body ) throws InvalidArgumentException {

            return parser.parse( context, response, body );

        }

    }
    
}
