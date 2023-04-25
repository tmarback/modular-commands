package dev.sympho.modular_commands.api.command.context;

import java.util.Objects;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import reactor.core.publisher.Mono;

/**
 * Context around the access of a program function by a user.
 *
 * @version 1.0
 * @since 1.0
 * @implSpec A context must be effectively constant; that is, an implementation of this interface
 *           must always return the same value on methods that return a direct value (such as
 *           {@link #getUser()} or {@link #getGuildId()}). Methods that fetch remote resources
 *           (i.e. that return a Mono) may return different objects over time (as the remote object
 *           is modified), but must always reference the same entity.
 */
public interface AccessContext {

    /**
     * Retrieves the backing client.
     *
     * @return The client.
     */
    @Pure
    GatewayDiscordClient getClient();

    /**
     * Retrieves the user.
     *
     * @return The user.
     */
    @Pure
    User getUser();

    /**
     * Retrieves the user as a guild member.
     *
     * @return The calling user as a guild member. May be empty
     *         if the context is a private channel.
     * @implSpec The default implementation delegates to {@link #getMember(Snowflake)}.
     *           It may (and <i>should</i>) be overriden to use existing instances when
     *           possible to avoid API calls.
     */
    @SideEffectFree
    default Mono<Member> getMember() {

        return getGuildId() == null ? Mono.empty() : getMember( getGuildId() );

    }

    /**
     * Retrieves the user as a guild member of the given guild.
     *
     * @param guildId The ID of the target guild.
     * @return The user as a guild member of the given guild.
     * @implNote The default implementation deletegates to 
     *           {@link #getUser()}.{@link User#asMember(Snowflake) asMember()}.
     *           It may (and <i>should</i>) be overriden to use existing instances when
     *           possible to avoid API calls.
     */
    @SideEffectFree
    default Mono<Member> getMember( final Snowflake guildId ) {

        return getUser().asMember( Objects.requireNonNull( guildId ) );

    }

    /**
     * Retrieves the guild, if there is one.
     *
     * @return The invoking guild.
     */
    @SideEffectFree
    Mono<Guild> getGuild();

    /**
     * Retrieves the ID of the guild, if there is one.
     *
     * @return The guild's ID, or {@code null} if a private channel.
     */
    @Pure
    @Nullable Snowflake getGuildId();

    /**
     * Determines if this context is a private channel.
     *
     * @return Whether the context is a private channel.
     */
    @Pure
    default boolean isPrivate() {

        return getGuildId() == null;

    }

    /**
     * Creates a copy of this context with the guild replaced by the given guild.
     * 
     * <p>All associated values are also replaced accordingly.
     *
     * @param guild The target guild. May be {@code null} to obtain a private-channel context.
     * @return The new context.
     */
    @SideEffectFree
    default AccessContext asGuild( final @Nullable Snowflake guild ) {

        if ( Objects.equals( guild, getGuildId() ) ) {
            return this;
        }

        return new GuildOverrideAccessContext( this, guild );

    }

    /**
     * Creates a copy of this context with the user replaced by the given user.
     * 
     * <p>All associated values are also replaced accordingly.
     *
     * @param user The target user.
     * @return The new context.
     */
    @SideEffectFree
    default AccessContext asUser( final User user ) {

        if ( Objects.equals( user.getId(), getUser().getId() ) ) {
            return this;
        }

        return new UserOverrideAccessContext<>( this, user );

    }
    
}
