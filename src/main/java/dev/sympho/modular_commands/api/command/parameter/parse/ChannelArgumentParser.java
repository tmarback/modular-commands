package dev.sympho.modular_commands.api.command.parameter.parse;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.dataflow.qual.Pure;

import discord4j.core.object.entity.channel.Channel;

/**
 * Parses Discord channels.
 *
 * @param <C> The channel type.
 * @param <T> The parsed type.
 * @version 1.0
 * @since 1.0
 */
public non-sealed interface ChannelArgumentParser<C extends Channel, T extends @NonNull Object>
        extends EntityArgumentParser<C, T> {

    /**
     * The required channel type.
     *
     * @return The channel type.
     */
    @Pure
    Class<C> type();
    
}
