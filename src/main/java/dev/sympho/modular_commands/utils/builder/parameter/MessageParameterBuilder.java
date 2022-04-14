package dev.sympho.modular_commands.utils.builder.parameter;

import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.parameter.MessageParameter;
import discord4j.core.object.entity.Message;

/**
 * Builder for a message parameter.
 *
 * @see MessageParameter
 * @version 1.0
 * @since 1.0
 */
public final class MessageParameterBuilder 
        extends EntityParameterBuilder<Message, MessageParameter, MessageParameterBuilder> {

    /**
     * Constructs a new builder with default values.
     */
    @Pure
    public MessageParameterBuilder() {}

    /**
     * Constructs a new builder that is a copy of the given builder.
     *
     * @param base The builder to copy.
     */
    @SideEffectFree
    public MessageParameterBuilder( final MessageParameterBuilder base ) {

        super( base );

    }

    /**
     * Constructs a new builder that is initialized to make a copy of 
     * the given parameter.
     *
     * @param base The parameter to copy.
     */
    @SideEffectFree
    public MessageParameterBuilder( final MessageParameter base ) {

        super( base );

    }

    @Override
    public MessageParameter build() throws IllegalStateException {

        try {
            return new MessageParameter( 
                    buildName(),
                    buildDescription(),
                    this.required, this.defaultValue );
        } catch ( final IllegalArgumentException e ) {
            throw new IllegalStateException( "Invalid parameter configuration.", e );
        }

    }
    
}
