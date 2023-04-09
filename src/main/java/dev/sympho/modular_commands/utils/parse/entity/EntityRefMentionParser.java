package dev.sympho.modular_commands.utils.parse.entity;

import java.util.regex.Pattern;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.nullness.util.NullnessUtil;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.context.CommandContext;
import dev.sympho.modular_commands.api.command.parameter.parse.InvalidArgumentException;
import dev.sympho.modular_commands.api.command.parameter.parse.ParserFunction;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Entity;
import reactor.core.publisher.Mono;

/**
 * A parser for mentions of Discord entities to references.
 *
 * @param <R> The reference type.
 * @version 1.0
 * @since 1.0
 */
public abstract class EntityRefMentionParser<R extends EntityRef<? extends @NonNull Entity>> 
        implements ParserFunction<String, R> {

    /** The pattern of a mention. */
    private static final Pattern MENTION_PATTERN = Pattern.compile( "<([^\\w\\s]{1,2}+)(\\d++)>" );

    /**
     * Determines if the mention prefix matches the expected type.
     *
     * @param prefix The detected prefix.
     * @return {@code true} if the prefix matches the expected, {@code false} otherwise.
     */
    @Pure
    protected abstract boolean prefixMatches( String prefix );

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
     * Gets the display name for this type.
     *
     * @return The name.
     * @apiNote For use in error messages.
     */
    @Pure
    protected abstract String typeName();

    /**
     * Parses the given string.
     *
     * @param raw The string to parse.
     * @return The ID parsed from the mention, or {@code null} if the given string is 
     *         not a mention.
     * @throws InvalidArgumentException if the given string is a mention but of the
     *                                  wrong type.
     */
    @SideEffectFree
    public @Nullable Snowflake parseId( final String raw ) throws InvalidArgumentException {

        final var matcher = MENTION_PATTERN.matcher( raw );
        if ( !matcher.matches() ) {
            return null;
        }

        final var prefix = NullnessUtil.castNonNull( matcher.group( 1 ) );
        final var id = NullnessUtil.castNonNull( matcher.group( 2 ) );

        if ( !prefixMatches( prefix ) ) {
            throw new InvalidArgumentException( "Not a %s mention".formatted( typeName() ) );
        }

        return Snowflake.of( id );

    }

    /**
     * Parses the given string.
     *
     * @param context The execution context.
     * @param raw The string to parse.
     * @return The reference parsed from the mention, or {@code null} if the given string is
     *         not a mention.
     * @throws InvalidArgumentException if the given string is a mention but of the
     *                                  wrong type.
     */
    @SideEffectFree
    public @Nullable R parseRef( final CommandContext context, final String raw ) 
            throws InvalidArgumentException {

        final var id = parseId( raw );
        return id == null ? null : makeRef( context, id );

    }

    @Override
    public Mono<R> parse( final CommandContext context, final String raw ) 
            throws InvalidArgumentException {

        final var ref = parseRef( context, raw );
        if ( ref == null ) {
            throw new InvalidArgumentException( "Not a mention" );
        }

        return Mono.just( ref );

    }
    
}
