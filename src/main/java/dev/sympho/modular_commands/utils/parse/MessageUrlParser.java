package dev.sympho.modular_commands.utils.parse;

import java.util.regex.Pattern;

import org.checkerframework.checker.nullness.util.NullnessUtil;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;

/**
 * A parser for message URLs.
 *
 * @version 1.0
 * @since 1.0
 */
public class MessageUrlParser extends EntityUrlParser<Message> {

    /** The path pattern. */
    private static final Pattern PATH_PATTERN = Pattern.compile(
            "/channels/\\d++/(\\d++)/(\\d++)" 
    );

    /** Creates a new instance. */
    public MessageUrlParser() {}

    @Override
    public String typeName() {

        return "message";

    }

    @Override
    protected boolean validPath( final String path ) {

        return PATH_PATTERN.matcher( path ).matches();

    }

    @Override
    protected Mono<Message> parsePath( final GatewayDiscordClient client, final String path ) {

        final var match = PATH_PATTERN.matcher( path );

        final String channelString = NullnessUtil.castNonNull( match.group( 1 ) );
        final String messageString = NullnessUtil.castNonNull( match.group( 2 ) );

        final Snowflake channelId = Snowflake.of( channelString );
        final Snowflake messageId = Snowflake.of( messageString );

        return client.getMessageById( channelId, messageId );

    }
    
}
