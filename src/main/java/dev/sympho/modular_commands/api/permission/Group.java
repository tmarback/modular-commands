package dev.sympho.modular_commands.api.permission;

import org.apache.commons.lang3.BooleanUtils;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.result.CommandResult;
import dev.sympho.modular_commands.api.command.result.UserNotAllowed;
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

    /**
     * Validates that the given caller belongs to this group in order to execute a command,
     * otherwise generating an error result.
     *
     * @param guild The guild that the command was invoked in.
     * @param channel The channel that the command was invoked in.
     * @param caller The caller that invoked the command.
     * @return A Mono that finishes empty if the user belongs to this group, or otherwise
     *         isses a {@link UserNotAllowed} result.
     * @apiNote This method is a convenience for command implementations that may need to
     *          apply additional restrictions based on transient state (or otherwise beyond
     *          what the command runtime can work with natively).
     */
    @SideEffectFree
    default Mono<CommandResult> validate( Mono<Guild> guild, Mono<MessageChannel> channel,
            User caller ) {

        return belongs( guild, channel, caller )
                .defaultIfEmpty( false )
                .filter( BooleanUtils::negate )
                .map( b -> new UserNotAllowed( this ) );

    }

}
