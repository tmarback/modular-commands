package dev.sympho.modular_commands.utils.parse.entity;

import dev.sympho.modular_commands.api.command.context.CommandContext;
import dev.sympho.modular_commands.utils.parse.entity.EntityRef.UserRef;
import discord4j.common.util.Snowflake;

/**
 * A parser that extracts a user reference from a string.
 *
 * @version 1.0
 * @since 1.0
 */
public class UserRefParser extends EntityRefParser<UserRef> {

    /** Creates a new instance. */
    public UserRefParser() {

        super( null, new UserRefMentionParser() );

    }

    @Override
    protected UserRef makeRef( final CommandContext context, final Snowflake id ) {

        return new UserRef( id );

    }
    
}
