package dev.sympho.modular_commands.utils.parse.entity;

import org.checkerframework.checker.nullness.qual.NonNull;

import dev.sympho.modular_commands.api.command.context.CommandContext;
import dev.sympho.modular_commands.api.command.parameter.parse.InvalidArgumentException;
import dev.sympho.modular_commands.api.command.parameter.parse.ParserFunction;
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

    /** The parser to delegate to. */
    private final EntityRefParser<? extends EntityRef<E>> parser;

    /**
     * Creates a new instance.
     *
     * @param parser The parser to delegate to.
     */
    protected EntityParser( final EntityRefParser<? extends EntityRef<E>> parser ) {

        this.parser = parser;

    }

    @Override
    public Mono<E> parse( final CommandContext context, final String raw )
            throws InvalidArgumentException {

        return parser.parseRef( context, raw ).get( context.client() );

    }
    
}
