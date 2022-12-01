package dev.sympho.modular_commands.utils.parse;

import org.checkerframework.checker.nullness.qual.Nullable;

import dev.sympho.modular_commands.api.command.context.CommandContext;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;

/**
 * A parser that extracts a message from a string.
 *
 * @version 1.0
 * @since 1.0
 */
public class MessageParser extends EntityParser<Message> {

    /** Link URL parser. */
    private static final MessageUrlParser URL_PARSER = new MessageUrlParser();

    /** Creates a new instance. */
    public MessageParser() {}

    @Override
    public @Nullable MessageUrlParser getUrlParser() {
        return URL_PARSER;
    }

    @Override
    protected Mono<Message> getEntity( final CommandContext context, final Snowflake id ) {

        return context.getChannel().flatMap( ch -> ch.getMessageById( id ) );

    }
    
}
