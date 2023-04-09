package dev.sympho.modular_commands.utils.parse.entity;

import discord4j.core.object.entity.Role;

/**
 * A parser that extracts a role from a string.
 *
 * @version 1.0
 * @since 1.0
 */
public class RoleParser extends EntityParser<Role> {

    /** Creates a new instance. */
    public RoleParser() {

        super( new RoleRefParser() );

    }
    
}
