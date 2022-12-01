package dev.sympho.modular_commands.utils.parse;

import java.util.Objects;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;

import dev.sympho.modular_commands.api.command.context.CommandContext;
import dev.sympho.modular_commands.api.command.parameter.parse.InvalidArgumentException;
import dev.sympho.modular_commands.utils.OptionalUtils;
import discord4j.common.util.Snowflake;
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

    /** The channel type. */
    private final Class<C> type;

    /** Link URL parser. */
    private final ChannelUrlParser<C> urlParser;

    /**
     * Creates a new instance.
     *
     * @param type The type of channel.
     */
    @Pure
    public ChannelParser( final Class<C> type ) {

        this.type = Objects.requireNonNull( type );
        this.urlParser = new ChannelUrlParser<>( type );

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
        
        return ChannelUrlParser.validate( channel, type );

    }

    @Override
    public @Nullable ChannelUrlParser<C> getUrlParser() {
        return urlParser;
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
