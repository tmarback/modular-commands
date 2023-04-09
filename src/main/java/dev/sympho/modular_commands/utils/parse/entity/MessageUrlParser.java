package dev.sympho.modular_commands.utils.parse.entity;

import discord4j.core.object.entity.Message;

/**
 * A parser for message URLs.
 *
 * @version 1.0
 * @since 1.0
 */
public class MessageUrlParser extends EntityUrlParser<Message> {

    /** Creates a new instance. */
    public MessageUrlParser() {

        super( new MessageRefUrlParser() );

    }
    
}
