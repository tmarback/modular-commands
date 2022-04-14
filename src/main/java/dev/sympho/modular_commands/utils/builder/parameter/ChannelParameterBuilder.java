package dev.sympho.modular_commands.utils.builder.parameter;

import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.parameter.ChannelParameter;
import dev.sympho.modular_commands.api.command.parameter.FloatParameter;
import discord4j.core.object.entity.channel.Channel;

/**
 * Builder for a floating-point parameter.
 *
 * @see FloatParameter
 * @version 1.0
 * @since 1.0
 */
public final class ChannelParameterBuilder 
        extends MentionParameterBuilder<Channel, ChannelParameter, ChannelParameterBuilder> {

    /**
     * Constructs a new builder with default values.
     */
    @Pure
    public ChannelParameterBuilder() {}

    /**
     * Constructs a new builder that is a copy of the given builder.
     *
     * @param base The builder to copy.
     */
    @SideEffectFree
    public ChannelParameterBuilder( final ChannelParameterBuilder base ) {

        super( base );

    }

    /**
     * Constructs a new builder that is initialized to make a copy of 
     * the given parameter.
     *
     * @param base The parameter to copy.
     */
    @SideEffectFree
    public ChannelParameterBuilder( final ChannelParameter base ) {

        super( base );

    }

    @Override
    public ChannelParameter build() throws IllegalStateException {

        try {
            return new ChannelParameter( 
                    buildName(),
                    buildDescription(),
                    this.required, this.defaultValue );
        } catch ( final IllegalArgumentException e ) {
            throw new IllegalStateException( "Invalid parameter configuration.", e );
        }

    }
    
}
