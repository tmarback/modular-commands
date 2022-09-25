package dev.sympho.modular_commands.utils.parse;

import java.util.Objects;
import java.util.regex.Pattern;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.util.NullnessUtil;
import org.checkerframework.dataflow.qual.Pure;

import dev.sympho.modular_commands.api.command.context.CommandContext;
import dev.sympho.modular_commands.api.command.parameter.parse.InvalidArgumentException;
import dev.sympho.modular_commands.utils.OptionalUtils;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.Channel;
import reactor.core.publisher.Mono;

/**
 * A parser that extracts a channel from a string.
 *
 * @param <C> The channel type.
 * @version 1.0
 * @since 1.0
 */
public class ChannelParser<C extends @NonNull Channel> extends MentionableParser<C> {

    /** Link URL pattern. */
    private static final Pattern LINK_PATTERN = Pattern.compile(
            "https://discord.com/channels/\\d+/(\\d+)"
    );

    /** The channel type. */
    private final Class<C> type;

    /**
     * Creates a new instance.
     *
     * @param type The type of channel.
     */
    @Pure
    public ChannelParser( final Class<C> type ) {

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
    public Mono<C> fromUrl( final GatewayDiscordClient client, final String url ) 
            throws InvalidArgumentException {
        
        final var matcher = LINK_PATTERN.matcher( url );
        if ( !matcher.matches() ) {
            throw new InvalidArgumentException( "Invalid channel URL: %s".formatted( url ) );
        }

        final String channelString = NullnessUtil.castNonNull( matcher.group( 1 ) );

        final Snowflake channelId = Snowflake.of( channelString );

        return client.getChannelById( channelId ).map( this::validate );

    }

    @Override
    protected Mono<C> getEntity( final CommandContext context, final Snowflake id ) {

        return context.getGuild().flatMap( g -> g.getChannelById( id ) ).map( this::validate );

    }

    @Override
    public String parseMention( final String mention ) throws InvalidArgumentException {

        return OptionalUtils.castPresent( MentionableParser.parseMention( mention, "#" ) )
                .orElseThrow( () -> new InvalidArgumentException(
                        "Not a valid channel mention: <%s>".formatted( mention ) )
                );

    }
    
}
