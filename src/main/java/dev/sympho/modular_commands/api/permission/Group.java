package dev.sympho.modular_commands.api.permission;

import org.checkerframework.dataflow.qual.SideEffectFree;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import reactor.core.publisher.Mono;

/**
 * Specifies a group that users may belong to.
 * 
 * <p>Note that group membership is not static, but rather may change between guilds
 * and/or channels. This allows the definition of flexible permission groups that refer 
 * to different groups of people in different servers/channels.
 *
 * @version 1.0
 * @since 1.0
 */
@FunctionalInterface
public interface Group {
    
    /**
     * Determines whether the given user belongs to this group in the context of the 
     * given guild and channel.
     *
     * @param guild The guild in which group membership is being checked for. It may be
     *              empty in the case of a private channel.
     * @param channel The channel in which group membership is being checked for.
     * @param user The user to check group membership for.
     * @return A Mono that issues {@code true} if the user belongs to this group under
     *         the given guild and channel, or {@code false} otherwise.
     */
    @SideEffectFree
    Mono<Boolean> belongs( Mono<Guild> guild, Mono<MessageChannel> channel, User user );

}
