package dev.sympho.modular_commands.execute;

import org.checkerframework.checker.nullness.qual.Nullable;

import discord4j.common.util.Snowflake;

/**
 * Object that provides the prefix to consider for commands during event handling.
 *
 * @version 1.0
 * @since 1.0
 */
public interface PrefixProvider {

    /**
     * Determines the prefix that should be used for the given guild.
     *
     * @param guildId The ID of the guild that the event was issued from, or {@code null}
     *                if it was issued from a private channel.
     * @return The prefix to use.
     */
    String getPrefix( @Nullable Snowflake guildId );
    
}
