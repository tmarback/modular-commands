package dev.sympho.modular_commands.utils.parse.entity;

import org.checkerframework.checker.nullness.qual.NonNull;

import dev.sympho.modular_commands.api.command.context.CommandContext;
import dev.sympho.modular_commands.utils.parse.entity.EntityRef.ChannelRef;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.channel.Channel;

/**
 * A parser that extracts a channel reference from a string.
 *
 * @param <C> The channel type.
 * @version 1.0
 * @since 1.0
 */
public class ChannelRefParser<C extends @NonNull Channel> extends EntityRefParser<ChannelRef<C>> {

    /** The channel type. */
    private final Class<C> type;

    /**
     * Creates a new instance.
     *
     * @param type The type of channel.
     */
    public ChannelRefParser( final Class<C> type ) {

        super( new ChannelRefUrlParser<>( type ), new ChannelRefMentionParser<>( type ) );

        this.type = type;

    }

    @Override
    protected ChannelRef<C> makeRef( final CommandContext context, final Snowflake id ) {

        return new ChannelRef<>( type, id );

    }
    
}
