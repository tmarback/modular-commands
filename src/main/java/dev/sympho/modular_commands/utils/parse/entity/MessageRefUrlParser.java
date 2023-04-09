package dev.sympho.modular_commands.utils.parse.entity;

import java.util.regex.Pattern;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.nullness.util.NullnessUtil;

import dev.sympho.modular_commands.utils.parse.entity.EntityRef.MessageRef;
import discord4j.common.util.Snowflake;

/**
 * A parser for message URLs.
 *
 * @version 1.0
 * @since 1.0
 */
public class MessageRefUrlParser extends EntityRefUrlParser<MessageRef> {

    /** The path pattern. */
    private static final Pattern PATH_PATTERN = Pattern.compile(
            "/channels/\\d++/(\\d++)/(\\d++)" 
    );

    /** Creates a new instance. */
    public MessageRefUrlParser() {}

    @Override
    public String typeName() {

        return "message";

    }

    @Override
    protected boolean validPath( final String path ) {

        return PATH_PATTERN.matcher( path ).matches();

    }

    @Override
    protected @Nullable MessageRef parsePath( final String path ) {

        final var match = PATH_PATTERN.matcher( path );
        if ( !match.matches() ) {
            return null;
        }

        final String channelString = NullnessUtil.castNonNull( match.group( 1 ) );
        final String messageString = NullnessUtil.castNonNull( match.group( 2 ) );

        final Snowflake channelId = Snowflake.of( channelString );
        final Snowflake messageId = Snowflake.of( messageString );

        return new MessageRef( messageId, channelId );

    }
    
}
