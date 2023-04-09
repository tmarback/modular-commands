package dev.sympho.modular_commands.utils.parse.entity;

import discord4j.core.object.entity.Message;

/**
 * A parser that extracts a message from a string.
 *
 * @version 1.0
 * @since 1.0
 */
public class MessageParser extends EntityParser<Message> {

    /** Creates a new instance. */
    public MessageParser() {

        super( new MessageRefParser() );

    }
    
}
