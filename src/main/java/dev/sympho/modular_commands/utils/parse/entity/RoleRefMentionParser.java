package dev.sympho.modular_commands.utils.parse.entity;

import org.checkerframework.checker.nullness.qual.Nullable;

import dev.sympho.modular_commands.api.command.context.CommandContext;
import dev.sympho.modular_commands.api.command.parameter.parse.InvalidArgumentException;
import dev.sympho.modular_commands.utils.parse.entity.EntityRef.RoleRef;
import discord4j.common.util.Snowflake;

/**
 * A parser for user mentions to references.
 * 
 * <p>Note that this parser does not support {@code @everyone} and {@code @here} in
 * {@link #parseId(String)}, as they do not have a unique ID. They are, however,
 * supported by {@link #parseRef(CommandContext, String)} and 
 * {@link #parse(CommandContext, String)}.
 *
 * @version 1.0
 * @since 1.0
 */
public class RoleRefMentionParser extends EntityRefMentionParser<RoleRef> {

    /** Creates a new instance. */
    public RoleRefMentionParser() {}

    @Override
    protected boolean prefixMatches( final String prefix ) {

        return "@&".equals( prefix );

    }

    @Override
    protected RoleRef makeRef( final CommandContext context, final Snowflake id ) {

        return new RoleRef( context, id );

    }

    @Override
    protected String typeName() {
        return "role";
    }

    @Override
    public @Nullable RoleRef parseRef( final CommandContext context, final String raw ) 
            throws InvalidArgumentException {

        if ( "@everyone".equals( raw ) || "@here".equals( raw ) ) {
            return new RoleRef( context, RoleRef.everyoneId( context ) );
        } else {
            return super.parseRef( context, raw );
        }

    }
    
}
