package dev.sympho.modular_commands.utils.parse;

import dev.sympho.modular_commands.api.command.context.CommandContext;
import dev.sympho.modular_commands.api.command.parameter.parse.InvalidArgumentException;
import dev.sympho.modular_commands.utils.OptionalUtils;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Role;
import reactor.core.publisher.Mono;

/**
 * A parser that extracts a role from a string.
 *
 * @version 1.0
 * @since 1.0
 */
public class RoleParser extends MentionableParser<Role> {

    /** Creates a new instance. */
    public RoleParser() {}

    @Override
    protected Mono<Role> getEntity( final CommandContext context, final Snowflake id ) {

        return context.getGuild().flatMap( g -> g.getRoleById( id ) );

    }

    @Override
    public String parseMention( final String mention ) throws InvalidArgumentException {

        return OptionalUtils.castPresent( MentionableParser.parseMention( mention, "@&" ) )
                .orElseThrow( () -> new InvalidArgumentException(
                        "Not a valid role mention: <%s>".formatted( mention ) )
                );

    }

    @Override
    public Mono<Role> parse( final CommandContext context, final String raw )
            throws InvalidArgumentException {

        if ( "@everyone".equals( raw ) || "@here".equals( raw ) ) {
            return context.getGuild().flatMap( Guild::getEveryoneRole );
        } else {
            return super.parse( context, raw );
        }

    }
    
}
