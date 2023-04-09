package dev.sympho.modular_commands.utils.parse.entity;

import discord4j.core.object.entity.User;

/**
 * A parser that extracts an user from a string.
 *
 * @version 1.0
 * @since 1.0
 */
public class UserParser extends EntityParser<User> {

    /** Creates a new instance. */
    public UserParser() {

        super( new UserRefParser() );

    }
    
}
