package dev.sympho.modular_commands.api.command.parameter;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.context.CommandContext;
import dev.sympho.modular_commands.api.exception.InvalidArgumentException;
import dev.sympho.modular_commands.utils.ParameterUtils;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.User;
import reactor.core.publisher.Mono;

/**
 * Specification for a parameter that receives a Discord role.
 *
 * @param name The name of the parameter.
 * @param description The description of the parameter.
 * @param required Whether the parameter must be specified to invoke the command.
 * @param defaultValue The default value for the parameter.
 * @version 1.0
 * @since 1.0
 */
public record UserParameter(
        String name, String description,
        boolean required, @Nullable User defaultValue
) implements MentionableParameter<User> {

    /**
     * Creates a new instance.
     *
     * @param name The name of the parameter.
     * @param description The description of the parameter.
     * @param required Whether the parameter must be specified to invoke the command.
     * @param defaultValue The default value for the parameter.
     */
    @SideEffectFree
    public UserParameter( 
            final String name, final String description, 
            final boolean required, final @Nullable User defaultValue
    ) {

        this.name = ParameterUtils.validateName( name );
        this.description = ParameterUtils.validateDescription( description );
        this.required = required;
        this.defaultValue = defaultValue;

    }

    @Override
    public Mono<User> getEntity( final CommandContext context, final Snowflake id ) {

        return context.getClient().getUserById( id );

    }

    @Override
    public String parseMention( final String mention ) throws InvalidArgumentException {

        if ( mention.startsWith( "@" ) ) {
            return mention.substring( 1 );
        } else if ( mention.startsWith( "@!" ) ) {
            return mention.substring( 2 );
        } else {
            throw new InvalidArgumentException( this, String.format( 
                "Not a valid role mention: <%s>", mention ) );
        }

    }
    
}
