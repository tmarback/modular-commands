package dev.sympho.modular_commands.api.command.parameter;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.context.CommandContext;
import dev.sympho.modular_commands.api.exception.InvalidArgumentException;
import dev.sympho.modular_commands.utils.OptionalUtils;
import dev.sympho.modular_commands.utils.ParameterUtils;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Role;
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
public record RoleParameter(
        String name, String description,
        boolean required, @Nullable Role defaultValue
) implements MentionableParameter<Role> {

    /**
     * Creates a new instance.
     *
     * @param name The name of the parameter.
     * @param description The description of the parameter.
     * @param required Whether the parameter must be specified to invoke the command.
     * @param defaultValue The default value for the parameter.
     */
    @SideEffectFree
    public RoleParameter( 
            final String name, final String description, 
            final boolean required, final @Nullable Role defaultValue
    ) {

        this.name = ParameterUtils.validateName( name );
        this.description = ParameterUtils.validateDescription( description );
        this.required = required;
        this.defaultValue = defaultValue;

    }

    @Override
    public Mono<Role> getEntity( final CommandContext context, final Snowflake id ) {

        return context.getGuild().flatMap( g -> g.getRoleById( id ) );

    }

    @Override
    public String parseMention( final String mention ) throws InvalidArgumentException {

        return OptionalUtils.castPresent( MentionableParameter.parseMention( mention, "@&" ) )
                .orElseThrow( () -> new InvalidArgumentException( this, 
                        "Not a valid role mention: <%s>".formatted( mention ) )
                );

    }

    @Override
    public Mono<Role> parse( final CommandContext context, final String raw )
            throws InvalidArgumentException {

        if ( "@everyone".equals( raw ) || "@here".equals( raw ) ) {
            return context.getGuild().flatMap( Guild::getEveryoneRole );
        } else {
            return MentionableParameter.super.parse( context, raw );
        }

    }
    
}
