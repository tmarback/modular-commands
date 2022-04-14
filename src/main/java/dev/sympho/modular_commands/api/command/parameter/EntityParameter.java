package dev.sympho.modular_commands.api.command.parameter;

import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.context.CommandContext;
import dev.sympho.modular_commands.api.exception.InvalidArgumentException;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
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
        permits MentionParameter, MessageParameter {

    /**
     * Parses the entity from a URL.
     *
     * @param client The client to get entities from.
     * @param url The URL to parse from.
     * @return The entity. May be empty if not found.
     * @throws InvalidArgumentException if the URL is invalid, or if the parameter type does not
     *                                  support URLs.
     * @implSpec The default implementation throws an exception (no URL support).
     */
    @SideEffectFree
    default Mono<T> fromUrl( final GatewayDiscordClient client, final String url ) 
            throws InvalidArgumentException {
        throw new InvalidArgumentException( this, "Links not supported." );
    }

    /**
     * Parses the entity ID from the raw argument.
     *
     * @param raw The received argument.
     * @return The entity ID.
     * @throws InvalidArgumentException if the given string is not a valid ID.
     */
    @SideEffectFree
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
     * @return The entity. May be empty if not found.
     */
    @SideEffectFree
    Mono<T> getEntity( CommandContext context, Snowflake id );

    @Override
    default Mono<T> parse( final CommandContext context, final String raw )
            throws InvalidArgumentException {

        final Mono<T> result;
        if ( raw.startsWith( "https://" ) ) {
            result = fromUrl( context.getClient(), raw );
        } else {
            final var id = parseId( raw );
            result = getEntity( context, id );
        }
        return result.onErrorMap( e -> new InvalidArgumentException( this, "Invalid.", e ) )
                .switchIfEmpty( 
                    Mono.error( () -> new InvalidArgumentException( this, "Not found." ) )
                );

    }
    
}
