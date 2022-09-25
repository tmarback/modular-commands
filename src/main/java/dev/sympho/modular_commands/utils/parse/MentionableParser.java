package dev.sympho.modular_commands.utils.parse;

import java.util.Optional;

import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.parameter.parse.InvalidArgumentException;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Entity;

/**
 * A parser that extracts a mentionable entity from a string.
 *
 * @param <E> The entity type.
 * @version 1.0
 * @since 1.0
 */
public abstract class MentionableParser<E extends Entity> extends EntityParser<E> {

    /**
     * Parses the entity ID from a text mention.
     *
     * @param mention The text mention.
     * @param prefix The prefix that identifies a mention.
     * @return The ID in the mention.
     * @throws InvalidArgumentException if the mention was invalid.
     */
    @SideEffectFree
    public static Optional<String> parseMention( final String mention, final String prefix ) 
            throws InvalidArgumentException {

        if ( mention.startsWith( prefix ) ) {
            return Optional.of( mention.substring( prefix.length() ) );
        } else {
            return Optional.empty();
        }

    }

    /**
     * Parses the entity ID from a text mention.
     *
     * @param mention The text mention.
     * @return The ID in the mention.
     * @throws InvalidArgumentException if the mention was invalid.
     */
    @SideEffectFree
    public abstract String parseMention( String mention ) throws InvalidArgumentException;

    /**
     * @implSpec Supports both plain IDs and mentions by default.
     */
    @Override
    public Snowflake extractId( final String raw ) throws InvalidArgumentException {

        final String id;
        if ( raw.startsWith( "<" ) && raw.endsWith( ">" ) ) {
            id = parseMention( raw.substring( 1, raw.length() - 1 ) );
        } else {
            id = raw;
        }

        return super.extractId( id );

    }
    
}
