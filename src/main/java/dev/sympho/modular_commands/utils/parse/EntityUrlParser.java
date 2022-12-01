package dev.sympho.modular_commands.utils.parse;

import java.net.URL;

import org.checkerframework.checker.nullness.qual.NonNull;
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
     * @return The parsed entity.
     */
    @SideEffectFree
    protected abstract Mono<E> parsePath( GatewayDiscordClient client, String path );

    @Override
    public boolean supported( final URL url ) {

        return "https".equals( url.getProtocol() ) 
                && "discord.com".equals( url.getHost() )
                && validPath( url.getPath() );

    }

    @Override
    public Mono<E> parse( final CommandContext context, final URL url ) 
            throws InvalidArgumentException {

        return parsePath( context.getClient(), url.getPath() );

    }
    
}
