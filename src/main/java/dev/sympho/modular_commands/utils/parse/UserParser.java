package dev.sympho.modular_commands.utils.parse;

import dev.sympho.modular_commands.api.command.context.CommandContext;
import dev.sympho.modular_commands.api.command.parameter.parse.InvalidArgumentException;
import dev.sympho.modular_commands.utils.OptionalUtils;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.User;
import reactor.core.publisher.Mono;

/**
 * A parser that extracts an user from a string.
 *
 * @version 1.0
 * @since 1.0
 */
public class UserParser extends MentionableParser<User> {

    /** Creates a new instance. */
    public UserParser() {}

    @Override
    protected Mono<User> getEntity( final CommandContext context, final Snowflake id ) {

        return context.getClient().getUserById( id );

    }

    @Override
    public String parseMention( final String mention ) throws InvalidArgumentException {

        return OptionalUtils.castPresent( 
                MentionableParser.parseMention( mention, "@" )
                .or( () -> MentionableParser.parseMention( mention, "@!" ) ) 
        ).orElseThrow( () -> new InvalidArgumentException( 
                "Not a valid user mention: <%s>".formatted( mention ) )
        );

    }
    
}
