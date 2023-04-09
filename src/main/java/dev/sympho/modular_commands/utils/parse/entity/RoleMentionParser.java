package dev.sympho.modular_commands.utils.parse.entity;

import discord4j.core.object.entity.Role;

/**
 * A parser for role mentions.
 *
 * @version 1.0
 * @since 1.0
 */
public class RoleMentionParser extends EntityMentionParser<Role> {

    /** Creates a new instance. */
    public RoleMentionParser() {

        super( new RoleRefMentionParser() );

    }
    
}
