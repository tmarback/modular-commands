package dev.sympho.modular_commands.utils.parse.entity;

import java.util.Objects;

import org.checkerframework.dataflow.qual.Pure;

import dev.sympho.modular_commands.api.command.context.CommandContext;
import dev.sympho.modular_commands.utils.parse.entity.EntityRef.ChannelRef;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.channel.Channel;

/**
 * A parser for channel mentions to references.
 *
 * @param <C> The channel type.
 * @version 1.0
 * @since 1.0
 */
public class ChannelRefMentionParser<C extends Channel> 
        extends EntityRefMentionParser<ChannelRef<C>> {

    /** The channel type. */
    private final Class<C> type;

    /**
     * Creates a new instance.
     *
     * @param type The type of channel.
     */
    @Pure
    public ChannelRefMentionParser( final Class<C> type ) {

        this.type = Objects.requireNonNull( type );

    }

    @Override
    protected boolean prefixMatches( final String prefix ) {

        return "#".equals( prefix );

    }

    @Override
    protected ChannelRef<C> makeRef( final CommandContext context, final Snowflake id ) {

        return new ChannelRef<>( type, id );

    }

    @Override
    protected String typeName() {
        return "channel";
    }
    
}
