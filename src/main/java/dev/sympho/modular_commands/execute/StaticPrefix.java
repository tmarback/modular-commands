package dev.sympho.modular_commands.execute;

import org.checkerframework.checker.nullness.qual.Nullable;

import discord4j.common.util.Snowflake;

/**
 * Prefix provider that only uses a static prefix determined at construction time.
 *
 * @param prefix The prefix to use.
 * @version 1.0
 * @since 1.0
 */
public record StaticPrefix( String prefix ) implements PrefixProvider {

    @Override
    public String getPrefix( final @Nullable Snowflake guildId ) {

        return prefix;

    }
    
}
