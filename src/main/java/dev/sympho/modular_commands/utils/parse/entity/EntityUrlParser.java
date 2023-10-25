package dev.sympho.modular_commands.utils.parse.entity;

import java.net.URL;
import java.util.Objects;

import org.checkerframework.checker.nullness.qual.NonNull;

import dev.sympho.modular_commands.api.command.context.CommandContext;
import dev.sympho.modular_commands.api.command.parameter.parse.InvalidArgumentException;
import dev.sympho.modular_commands.utils.parse.UrlParser;
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

    /** The parser to obtain the reference with. */
    private final EntityRefUrlParser<? extends EntityRef<E>> refParser;

    /**
     * Creates a new instance.
     *
     * @param refParser The parser to obtain the reference with.
     */
    protected EntityUrlParser( final EntityRefUrlParser<? extends EntityRef<E>> refParser ) {

        this.refParser = Objects.requireNonNull( refParser );

    }

    @Override
    public boolean supports( final URL url ) {

        return refParser.supports( url );

    }

    @Override
    public Mono<E> parse( final CommandContext context, final URL url ) 
            throws InvalidArgumentException {

        return refParser.parse( url ).get( context.client() );

    }
    
}
