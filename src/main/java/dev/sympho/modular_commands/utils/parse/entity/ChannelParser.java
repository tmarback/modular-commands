package dev.sympho.modular_commands.utils.parse.entity;

import org.checkerframework.checker.nullness.qual.NonNull;

import discord4j.core.object.entity.channel.Channel;

/**
 * A parser that extracts a channel from a string.
 *
 * @param <C> The channel type.
 * @version 1.0
 * @since 1.0
 */
public class ChannelParser<C extends @NonNull Channel> extends EntityParser<C> {

    /**
     * Creates a new instance.
     *
     * @param type The type of channel.
     */
    public ChannelParser( final Class<C> type ) {

        super( new ChannelRefParser<>( type ) );

    }
    
}
