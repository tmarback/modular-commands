package dev.sympho.modular_commands.utils.parse;

import java.util.Objects;
import java.util.regex.Pattern;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.util.NullnessUtil;
import org.checkerframework.dataflow.qual.Pure;

import dev.sympho.modular_commands.api.command.parameter.parse.InvalidArgumentException;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.Channel;
import reactor.core.publisher.Mono;

/**
 * A parser for channel URLs.
 *
 * @param <C> The channel type.
 * @version 1.0
 * @since 1.0
 */
public class ChannelUrlParser<C extends @NonNull Channel> extends EntityUrlParser<C> {

    /** The path pattern. */
    private static final Pattern PATH_PATTERN = Pattern.compile( "/channels/\\d+/(\\d+)" );

    /** The channel type. */
    private final Class<C> type;

    /**
     * Creates a new instance.
     *
     * @param type The type of channel.
     */
    @Pure
    public ChannelUrlParser( final Class<C> type ) {

        this.type = Objects.requireNonNull( type );

    }

    /**
     * Validates that a channel is of the given type.
     *
     * @param <C> The channel type.
     * @param channel The channel.
     * @param type The channel type.
     * @return The channel.
     * @throws InvalidArgumentException if the channel does not match the given type.
     */
    @Pure
    public static <C extends @NonNull Channel> C validate( final Channel channel, 
            final Class<C> type ) throws InvalidArgumentException {

        try {
            return type.cast( channel );
        } catch ( final ClassCastException e ) {
            throw new InvalidArgumentException( "Channel must be a %s".formatted( 
                    type.getSimpleName() ) );
        }

    }

    /**
     * Validates that a channel is of the expected type.
     *
     * @param channel The channel.
     * @return The channel.
     * @throws InvalidArgumentException if the channel does not match the expected type.
     */
    @Pure
    private C validate( final Channel channel ) throws InvalidArgumentException {
        
        return validate( channel, type );

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
    protected Mono<C> parsePath( final GatewayDiscordClient client, final String path ) {

        final var match = PATH_PATTERN.matcher( path );

        final String channelString = NullnessUtil.castNonNull( match.group( 1 ) );

        final Snowflake channelId = Snowflake.of( channelString );

        return client.getChannelById( channelId ).map( this::validate );

    }
    
}
