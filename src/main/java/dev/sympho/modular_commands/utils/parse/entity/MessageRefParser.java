package dev.sympho.modular_commands.utils.parse.entity;

import dev.sympho.modular_commands.api.command.context.CommandContext;
import dev.sympho.modular_commands.utils.parse.entity.EntityRef.MessageRef;
import discord4j.common.util.Snowflake;

/**
 * A parser that extracts a message reference from a string.
 *
 * @version 1.0
 * @since 1.0
 */
public class MessageRefParser extends EntityRefParser<MessageRef> {

    /** Creates a new instance. */
    public MessageRefParser() {

        super( new MessageRefUrlParser(), null );

    }

    @Override
    protected MessageRef makeRef( final CommandContext context, final Snowflake id ) {

        return new MessageRef( context, id );

    }
    
}
