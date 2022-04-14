package dev.sympho.modular_commands.api.command.parameter;

import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.exception.InvalidArgumentException;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Entity;

/**
 * Specification for a parameter that receives Discord entities that can be mentioned.
 *
 * @param <T> The entity type.
 * @version 1.0
 * @since 1.0
 */
public non-sealed interface MentionableParameter<T extends Entity> extends EntityParameter<T> {

    /**
     * Parses the entity ID from a text mention.
     *
     * @param mention The text mention.
     * @return The ID in the mention.
     * @throws InvalidArgumentException if the mention was invalid.
     */
    @SideEffectFree
    String parseMention( String mention ) throws InvalidArgumentException;

    @Override
    default Snowflake parseId( final String raw ) throws InvalidArgumentException {

        final String id;
        if ( raw.startsWith( "<" ) && raw.endsWith( ">" ) ) {
            id = parseMention( raw.substring( 1, raw.length() - 1 ) );
        } else {
            id = raw;
        }

        return EntityParameter.super.parseId( id );

    }
    
}
