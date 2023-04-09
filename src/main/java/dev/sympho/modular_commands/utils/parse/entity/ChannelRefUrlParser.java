package dev.sympho.modular_commands.utils.parse.entity;

import java.util.Objects;
import java.util.regex.Pattern;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.nullness.util.NullnessUtil;
import org.checkerframework.dataflow.qual.Pure;

import dev.sympho.modular_commands.utils.parse.entity.EntityRef.ChannelRef;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.channel.Channel;

/**
 * A reference parser for channel URLs.
 *
 * @param <C> The channel type.
 * @version 1.0
 * @since 1.0
 */
public class ChannelRefUrlParser<C extends @NonNull Channel> 
        extends EntityRefUrlParser<ChannelRef<C>> {

    /** The path pattern. */
    private static final Pattern PATH_PATTERN = Pattern.compile( "/channels/\\d++/(\\d++)" );

    /** The channel type. */
    private final Class<C> type;

    /**
     * Creates a new instance.
     *
     * @param type The type of channel.
     */
    @Pure
    public ChannelRefUrlParser( final Class<C> type ) {

        this.type = Objects.requireNonNull( type );

    }

    @Override
    public String typeName() {

        return "channel";

    }

    @Override
    protected boolean validPath( final String path ) {

        return PATH_PATTERN.matcher( path ).matches();

    }

    @Override
    protected @Nullable ChannelRef<C> parsePath( final String path ) {

        final var match = PATH_PATTERN.matcher( path );
        if ( !match.matches() ) {
            return null;
        }

        final String channelString = NullnessUtil.castNonNull( match.group( 1 ) );

        final Snowflake channelId = Snowflake.of( channelString );

        return new ChannelRef<>( type, channelId );

    }
    
}
