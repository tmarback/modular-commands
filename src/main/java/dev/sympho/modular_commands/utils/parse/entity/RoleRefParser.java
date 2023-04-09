package dev.sympho.modular_commands.utils.parse.entity;

import dev.sympho.modular_commands.api.command.context.CommandContext;
import dev.sympho.modular_commands.api.command.parameter.parse.InvalidArgumentException;
import dev.sympho.modular_commands.utils.parse.entity.EntityRef.RoleRef;
import discord4j.common.util.Snowflake;

/**
 * A parser that extracts a role reference from a string.
 *
 * @version 1.0
 * @since 1.0
 */
public class RoleRefParser extends EntityRefParser<RoleRef> {

    /** Creates a new instance. */
    public RoleRefParser() {

        super( null, new RoleRefMentionParser() );

    }

    @Override
    protected RoleRef makeRef( final CommandContext context, final Snowflake id ) {

        return new RoleRef( context, id );

    }

    @Override
    public RoleRef parseRef( final CommandContext context, final String raw ) 
            throws InvalidArgumentException {

        if ( "everyone".equals( raw ) || "here".equals( raw ) ) {
            return new RoleRef( context, RoleRef.everyoneId( context ) );
        } else {
            return super.parseRef( context, raw );
        }

    }
    
}
