package dev.sympho.modular_commands.utils.parse.entity;

import java.util.Objects;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.dataflow.qual.Pure;

import dev.sympho.modular_commands.api.command.context.CommandContext;
import dev.sympho.modular_commands.api.command.parameter.parse.InvalidArgumentException;
import dev.sympho.modular_commands.api.command.parameter.parse.ParserFunction;
import discord4j.core.object.entity.Entity;
import reactor.core.publisher.Mono;

/**
 * A parser for mentions of Discord entities.
 *
 * @param <E> The entity type.
 * @version 1.0
 * @since 1.0
 */
public class EntityMentionParser<E extends @NonNull Entity> implements ParserFunction<String, E> {

    /** The parser to obtain the reference with. */
    private final EntityRefMentionParser<? extends EntityRef<E>> refParser;

    /**
     * Creates a new instance.
     *
     * @param refParser The parser to obtain the reference with.
     */
    @Pure
    protected EntityMentionParser( 
            final EntityRefMentionParser<? extends EntityRef<E>> refParser ) {

        this.refParser = Objects.requireNonNull( refParser );

    }

    @Override
    public Mono<E> parse( final CommandContext context, final String raw ) 
            throws InvalidArgumentException {

        return refParser.parse( context, raw ).flatMap( ref -> ref.get( context.client() ) );

    }
    
}
