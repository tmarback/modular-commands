package dev.sympho.modular_commands.utils.parse;

import java.util.regex.Pattern;

import org.checkerframework.checker.nullness.util.NullnessUtil;

import dev.sympho.modular_commands.api.command.context.CommandContext;
import dev.sympho.modular_commands.api.command.parameter.parse.InvalidArgumentException;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;

/**
 * A parser that extracts a message from a string.
 *
 * @version 1.0
 * @since 1.0
 */
public class MessageParser extends EntityParser<Message> {

    /** Link URL pattern. */
    private static final Pattern LINK_PATTERN = Pattern.compile(
            "https://discord.com/channels/\\d+/(\\d+)/(\\d+)"
    );

    /** Creates a new instance. */
    public MessageParser() {}

    @Override
    public Mono<Message> fromUrl( final GatewayDiscordClient client, final String url ) 
            throws InvalidArgumentException {
        
        final var matcher = LINK_PATTERN.matcher( url );
        if ( !matcher.matches() ) {
            throw new InvalidArgumentException( "Invalid channel URL: %s".formatted( url ) );
        }

        final String channelString = NullnessUtil.castNonNull( matcher.group( 1 ) );
        final String messageString = NullnessUtil.castNonNull( matcher.group( 2 ) );

        final Snowflake channelId = Snowflake.of( channelString );
        final Snowflake messageId = Snowflake.of( messageString );

        return client.getMessageById( channelId, messageId );

    }

    @Override
    protected Mono<Message> getEntity( final CommandContext context, final Snowflake id ) {

        return context.getChannel().flatMap( ch -> ch.getMessageById( id ) );

    }
    
}
