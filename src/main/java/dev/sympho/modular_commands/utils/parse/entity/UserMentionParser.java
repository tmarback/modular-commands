package dev.sympho.modular_commands.utils.parse.entity;

import discord4j.core.object.entity.User;

/**
 * A parser for user mentions.
 *
 * @version 1.0
 * @since 1.0
 */
public class UserMentionParser extends EntityMentionParser<User> {

    /** Creates a new instance. */
    public UserMentionParser() {

        super( new UserRefMentionParser() );

    }
    
}
