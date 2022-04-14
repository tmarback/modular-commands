package dev.sympho.modular_commands.api.command.parameter;

import dev.sympho.modular_commands.api.command.context.CommandContext;
import dev.sympho.modular_commands.api.exception.InvalidArgumentException;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Entity;
import reactor.core.publisher.Mono;

/**
 * Specification for a parameter that receives Discord entities.
 *
 * @param <T> The entity type.
 * @version 1.0
 * @since 1.0
 */
public sealed interface EntityParameter<T extends Entity> extends Parameter<T>
        permits MentionableParameter {

    /**
     * Parses the entity ID from the raw argument.
     *
     * @param raw The received argument.
     * @return The entity ID.
     * @throws InvalidArgumentException if the given string is not a valid ID.
     */
    default Snowflake parseId( final String raw ) throws InvalidArgumentException {

        try {
            return Snowflake.of( raw );
        } catch ( NumberFormatException e ) {
            throw new InvalidArgumentException( this, 
                    String.format( "Value '%s' is not a valid snowflake ID.", raw ) );
        }

    }

    /**
     * Retrieves the entity with the given ID.
     *
     * @param context The execution context.
     * @param id The entity ID.
     * @return The entity.
     */
    Mono<T> getEntity( CommandContext context, Snowflake id );

    @Override
    default Mono<T> parse( final CommandContext context, final String raw )
            throws InvalidArgumentException {

        final var id = parseId( raw );
        return getEntity( context, id );

    }
    
}
