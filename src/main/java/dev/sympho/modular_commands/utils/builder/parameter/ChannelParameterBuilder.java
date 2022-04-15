package dev.sympho.modular_commands.utils.builder.parameter;

import java.util.Objects;

import org.checkerframework.dataflow.qual.Deterministic;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.parameter.ChannelParameter;
import discord4j.core.object.entity.channel.Channel;

/**
 * Builder for a channel parameter.
 *
 * @see ChannelParameter
 * @version 1.0
 * @since 1.0
 */
@SuppressWarnings( "checkstyle:hiddenfield" )
public final class ChannelParameterBuilder 
        extends MentionableParameterBuilder<Channel, ChannelParameter, ChannelParameterBuilder> {

    /** The channel type. */
    private Class<? extends Channel> type;

    /**
     * Constructs a new builder with default values.
     */
    @Pure
    public ChannelParameterBuilder() {

        this.type = Channel.class;

    }

    /**
     * Constructs a new builder that is a copy of the given builder.
     *
     * @param base The builder to copy.
     */
    @SideEffectFree
    public ChannelParameterBuilder( final ChannelParameterBuilder base ) {

        super( base );
        this.type = base.type;

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
        this.type = base.getType();

    }

    /**
     * Sets the channel type.
     * 
     * <p>Defaults to {@link Channel}, so any channel type.
     *
     * @param type The channel type.
     * @return This builder.
     */
    @Deterministic
    public ChannelParameterBuilder withType( final Class<? extends Channel> type ) {

        this.type = Objects.requireNonNull( type );
        return this;

    }

    @Override
    public ChannelParameter build() throws IllegalStateException {

        try {
            return new ChannelParameter( 
                    buildName(),
                    buildDescription(),
                    this.required, this.defaultValue, type );
        } catch ( final IllegalArgumentException e ) {
            throw new IllegalStateException( "Invalid parameter configuration.", e );
        }

    }
    
}
