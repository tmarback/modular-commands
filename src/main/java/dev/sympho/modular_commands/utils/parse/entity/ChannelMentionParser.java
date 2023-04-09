package dev.sympho.modular_commands.utils.parse.entity;

import org.checkerframework.checker.nullness.qual.NonNull;

import discord4j.core.object.entity.channel.Channel;

/**
 * A parser for channel mentions.
 *
 * @param <C> The channel type.
 * @version 1.0
 * @since 1.0
 */
public class ChannelMentionParser<C extends @NonNull Channel> extends EntityMentionParser<C> {

    /**
     * Creates a new instance.
     *
     * @param type The type of channel.
     */
    public ChannelMentionParser( final Class<C> type ) {

        super( new ChannelRefMentionParser<>( type ) );

    }
    
}
