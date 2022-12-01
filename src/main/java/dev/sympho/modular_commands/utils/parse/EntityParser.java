package dev.sympho.modular_commands.utils.parse;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.context.CommandContext;
import dev.sympho.modular_commands.api.command.parameter.parse.InvalidArgumentException;
import dev.sympho.modular_commands.api.command.parameter.parse.ParserFunction;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Entity;
import reactor.core.publisher.Mono;

/**
 * A parser that extracts an entity from a string.
 *
 * @param <E> The entity type.
 * @version 1.0
 * @since 1.0
 */
public abstract class EntityParser<E extends @NonNull Entity> implements ParserFunction<String, E> {

    /**
     * Parses a snowflake ID from an argument string.
     *
     * @param raw The argument.
     * @return The snowflake ID.
     * @throws InvalidArgumentException if the given string is not a valid snowflake.
     */
    @SideEffectFree
    public static Snowflake parseId( final String raw ) throws InvalidArgumentException {

        try {
            return Snowflake.of( raw );
        } catch ( final NumberFormatException e ) {
            throw new InvalidArgumentException(
                    String.format( "Value '%s' is not a valid snowflake ID.", raw ) );
        }

    }

    /**
     * Retrieves the parser to use for URLs, if supported.
     *
     * @return The URL parser, or {@code null} if URLs are not supported.
     * @implSpec The default returns {@code null}.
     */
    @Pure
    public @Nullable EntityUrlParser<E> getUrlParser() {
        return null;
    }

    /**
     * Parses the entity ID from the raw argument.
     *
     * @param raw The received argument.
     * @return The entity ID.
     * @throws InvalidArgumentException if the given string is not a valid ID.
     * @implSpec Supports only plain IDs by default.
     */
    @SideEffectFree
    public Snowflake extractId( final String raw ) throws InvalidArgumentException {
        return parseId( raw );
    }

    /**
     * Retrieves the entity with the given ID.
     *
     * @param context The execution context.
     * @param id The entity ID.
     * @return The entity. May be empty if not found.
     */
    @SideEffectFree
    protected abstract Mono<E> getEntity( CommandContext context, Snowflake id );

    @Override
    public Mono<E> parse( final CommandContext context, final String raw )
            throws InvalidArgumentException {

        if ( getUrlParser() != null && raw.startsWith( "https://" ) ) {
            return getUrlParser().parse( context, raw );
        }

        return getEntity( context, extractId( raw ) )
                .onErrorMap( e -> new InvalidArgumentException( "Invalid.", e ) )
                .switchIfEmpty( 
                    Mono.error( () -> new InvalidArgumentException( "Not found." ) )
                );

    }
    
}
