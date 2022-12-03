package dev.sympho.modular_commands.utils.parse;

import java.net.URL;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.context.CommandContext;
import dev.sympho.modular_commands.api.command.parameter.parse.InvalidArgumentException;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Entity;
import reactor.core.publisher.Mono;

/**
 * A parser for URLs of Discord entities.
 *
 * @param <E> The entity type.
 * @version 1.0
 * @since 1.0
 */
public abstract class EntityUrlParser<E extends @NonNull Entity> implements UrlParser<E> {

    /**
     * Checks if the given path (endpoint) is valid for the entity type of this parser.
     *
     * @param path The path.
     * @return If the path is valid for this parser.
     */
    @Pure
    protected abstract boolean validPath( String path );

    /**
     * Parses the URL path.
     *
     * @param client The Discord client.
     * @param path The path.
     * @return The parsed entity, or {@code null} if the path is not valid.
     */
    @SideEffectFree
    protected abstract @Nullable Mono<E> parsePath( GatewayDiscordClient client, String path );

    /**
     * Gets the display name for this type.
     *
     * @return The name.
     * @apiNote For use in error messages.
     */
    @Pure
    protected abstract String typeName();

    /**
     * Checks if the URL is a valid (generic) Discord URL.
     *
     * @param url The URL to check.
     * @return If it is a valid Discord URL.
     */
    @Pure
    private boolean baseValid( final URL url ) {

        return UrlParserUtils.isHttps( url )
                && UrlParserUtils.isHost( url, "discord.com" );

    }

    @Override
    public boolean supports( final URL url ) {

        return baseValid( url ) && validPath( url.getPath() );

    }

    @Override
    public Mono<E> parse( final CommandContext context, final URL url ) 
            throws InvalidArgumentException {

        if ( !baseValid( url ) ) {
            throw new InvalidArgumentException( "Not a valid Discord URL: %s".formatted( url ) );
        }

        final var res = parsePath( context.getClient(), url.getPath() );
        if ( res == null ) {
            throw new InvalidArgumentException( "Not a valid %s URL: %s".formatted( 
                    typeName(), url ) );
        } else {
            return res;
        }

    }
    
}
