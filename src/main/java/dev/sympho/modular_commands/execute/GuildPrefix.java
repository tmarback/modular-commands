package dev.sympho.modular_commands.execute;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.checkerframework.checker.nullness.qual.Nullable;

import discord4j.common.util.Snowflake;

/**
 * Prefix provider that allows prefix overrides per guild while falling back to
 * a default construction-time prefix if no override.
 * 
 * <p>Private channels are treated as guilds with no override, so the default
 * prefix is used in their case.
 *
 * @param prefixes The prefixes per guild, keyed by the guild ID.
 * @param defaultPrefix The default prefix.
 * @version 1.0
 * @since 1.0
 */
public record GuildPrefix( Map<Snowflake, String> prefixes, String defaultPrefix )
        implements PrefixProvider {

    /**
     * Creates a new instance with the given set of initial overrides.
     *
     * @param prefixes The initial prefixes per guild, keyed by the guild ID.
     * @param defaultPrefix The default prefix.
     */
    public GuildPrefix( final Map<Snowflake, String> prefixes, final String defaultPrefix ) {

        this.prefixes = new ConcurrentHashMap<>( prefixes );
        this.defaultPrefix = defaultPrefix;

    }

    /**
     * Creates a new instance with no overrides.
     *
     * @param defaultPrefix The default prefix.
     */
    public GuildPrefix( final String defaultPrefix ) {

        this( Collections.emptyMap(), defaultPrefix );

    }

    /**
     * Sets a prefix override for a guild.
     *
     * @param guildId The guild to set the override for.
     * @param prefix The prefix to use for that guild.
     */
    public void setPrefix( final Snowflake guildId, final String prefix ) {

        prefixes.put( guildId, prefix );

    }

    /**
     * Clears the override for a guild, if there is one.
     *
     * @param guildId The guild to reset the prefix override for.
     * @return {@code true} if the prefix was reset for the given guild.
     *         {@code false} if the given guild did not have a prefix override.
     */
    public boolean clearPrefix( final Snowflake guildId ) {

        return prefixes.remove( guildId ) != null;

    }

    @Override
    public String getPrefix( final @Nullable Snowflake guildId ) {

        if ( guildId != null ) {
            return prefixes.getOrDefault( guildId, defaultPrefix );
        } else {
            return defaultPrefix;
        }

    }
    
}
