package dev.sympho.modular_commands.utils.parse.entity;

import dev.sympho.modular_commands.api.command.context.CommandContext;
import dev.sympho.modular_commands.utils.parse.entity.EntityRef.UserRef;
import discord4j.common.util.Snowflake;

/**
 * A parser for user mentions to references.
 *
 * @version 1.0
 * @since 1.0
 */
public class UserRefMentionParser extends EntityRefMentionParser<UserRef> {

    /** Creates a new instance. */
    public UserRefMentionParser() {}

    @Override
    protected boolean prefixMatches( final String prefix ) {

        return "@".equals( prefix ) || "@!".equals( prefix );

    }

    @Override
    protected UserRef makeRef( final CommandContext context, final Snowflake id ) {

        return new UserRef( id );

    }

    @Override
    protected String typeName() {
        return "user";
    }
    
}
