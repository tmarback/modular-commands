package dev.sympho.modular_commands.utils.parse.entity;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.dataflow.qual.Pure;

import discord4j.core.object.entity.channel.Channel;

/**
 * A parser for channel URLs.
 *
 * @param <C> The channel type.
 * @version 1.0
 * @since 1.0
 */
public class ChannelUrlParser<C extends @NonNull Channel> extends EntityUrlParser<C> {

    /**
     * Creates a new instance.
     *
     * @param type The type of channel.
     */
    @Pure
    public ChannelUrlParser( final Class<C> type ) {

        super( new ChannelRefUrlParser<>( type ) );

    }
    
}
