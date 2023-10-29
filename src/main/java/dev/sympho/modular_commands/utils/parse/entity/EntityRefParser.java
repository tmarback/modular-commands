package dev.sympho.modular_commands.utils.parse.entity;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.context.CommandContext;
import dev.sympho.modular_commands.api.command.parameter.parse.InvalidArgumentException;
import dev.sympho.modular_commands.api.command.parameter.parse.ParserFunction;
import dev.sympho.modular_commands.utils.parse.UrlParser;
import dev.sympho.modular_commands.utils.parse.UrlParserUtils;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Entity;
import reactor.core.publisher.Mono;

/**
 * A parser that extracts an entity reference from a string.
 *
 * @param <R> The reference type.
 * @version 1.0
 * @since 1.0
 */
public abstract class EntityRefParser<R extends EntityRef<? extends @NonNull Entity>> 
        implements ParserFunction<String, R> {

    /** The parser to use for URLs. May be {@code null} if the entity does not support URLs. */
    private final @MonotonicNonNull EntityRefUrlParser<R> urlParser;

    /** The parser to use for mentions. May be {@code null} if the entity does not support URLs. */
    private final @MonotonicNonNull EntityRefMentionParser<R> mentionParser;

    /**
     * Creates a new instance.
     *
     * @param urlParser The parser to use for URLs. May be {@code null} if the entity does 
     *                  not support URLs.
     * @param mentionParser The parser to use for mentions. May be {@code null} if the entity does
     *                      not support URLs.
     */
    @Pure
    protected EntityRefParser( 
            final @Nullable EntityRefUrlParser<R> urlParser, 
            final @Nullable EntityRefMentionParser<R> mentionParser 
    ) {

        this.urlParser = urlParser;
        this.mentionParser = mentionParser;

    }

    /**
     * Creates the reference.
     *
     * @param context The execution context.
     * @param id The parsed ID.
     * @return The created reference.
     */
    @SideEffectFree
    protected abstract R makeRef( CommandContext context, Snowflake id );

    /**
     * Parses the given string.
     *
     * @param context The execution context.
     * @param raw The string to parse.
     * @return The parsed reference.
     * @throws InvalidArgumentException if the given string does not contain a valid reference.
     */
    @SideEffectFree
    public R parseRef( final CommandContext context, final String raw ) 
            throws InvalidArgumentException {

        if ( urlParser != null ) {
            final var url = UrlParser.parseUrl( raw, UrlParserUtils.PROTOCOL_HTTPS );
            if ( url != null ) {
                return urlParser.parse( url );
            }
        }

        if ( mentionParser != null ) {
            final var ref = mentionParser.parseRef( context, raw );
            if ( ref != null ) {
                return ref;
            }
        }

        final Snowflake id;
        try {
            id = Snowflake.of( raw );
        } catch ( final NumberFormatException ex ) {
            throw new InvalidArgumentException( "Not a valid ID: " + raw );
        }

        return makeRef( context, id );

    }

    @Override
    public Mono<R> parse( final CommandContext context, final String raw ) 
            throws InvalidArgumentException {

        return Mono.just( parseRef( context, raw ) );

    }
    
}
