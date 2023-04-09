package dev.sympho.modular_commands.utils.parse.entity;

import java.net.URL;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.context.CommandContext;
import dev.sympho.modular_commands.api.command.parameter.parse.InvalidArgumentException;
import dev.sympho.modular_commands.utils.parse.UrlParser;
import dev.sympho.modular_commands.utils.parse.UrlParserUtils;
import discord4j.core.object.entity.Entity;
import reactor.core.publisher.Mono;

/**
 * A parser for URLs of Discord entities to references.
 *
 * @param <R> The reference type.
 * @version 1.0
 * @since 1.0
 */
public abstract class EntityRefUrlParser<R extends EntityRef<? extends @NonNull Entity>> 
        implements UrlParser<R> {

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
     * @param path The path.
     * @return The parsed entity, or {@code null} if the path is not valid.
     */
    @SideEffectFree
    protected abstract @Nullable R parsePath( String path );

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

    /**
     * Parses the given URL into the corresponding reference.
     *
     * @param url The URL to parse.
     * @return The parsed reference. May fail with a {@link InvalidArgumentException} if not
     *         a valid reference URL.
     * @throws InvalidArgumentException if not a valid reference URL.
     */
    @SideEffectFree
    public final R parse( final URL url ) throws InvalidArgumentException {

        if ( !baseValid( url ) ) {
            throw new InvalidArgumentException( "Not a valid Discord URL: %s".formatted( url ) );
        }

        final var res = parsePath( url.getPath() );
        if ( res == null ) {
            throw new InvalidArgumentException( "Not a valid %s URL: %s".formatted( 
                    typeName(), url ) );
        } else {
            return res;
        }

    }

    /**
     * Parses the given string as a reference URL.
     *
     * @param raw The string to parse.
     * @return The parsed reference.
     * @throws InvalidArgumentException if the given string is not a valid reference URL.
     */
    @SideEffectFree
    public final R parse( final String raw ) throws InvalidArgumentException {

        return parse( UrlParser.getUrl( raw ) );

    }

    @Override
    public final Mono<R> parse( final CommandContext context, final URL url ) 
            throws InvalidArgumentException {

        return Mono.just( parse( url ) );

    }
    
}
