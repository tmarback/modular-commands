package dev.sympho.modular_commands.api.command.parameter.parse;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.dataflow.qual.Pure;

import dev.sympho.modular_commands.utils.parse.entity.EntityRefParser;
import discord4j.common.util.Snowflake;

/**
 * Parses snowflake-based input arguments.
 * 
 * <p>Note that no validation is performed on the raw value received ahead of time.
 * The benefit offered over using a {@link StringParser} with a snowflake or 
 * entity-id parser function is that, for hybrid commands (text and slash) that need
 * only a plain ID (instead of a proper entity object), this type can appear in the
 * slash command as a proper type (user/role/channel), which also saves on parsing
 * cost, while avoiding the need to fetch the entity separately in the text command, 
 * losing only the validation.
 * 
 * <p>Note that, when used in a message-based invocation, a parser with a type other than 
 * {@link Type#ANY} accepts the same formats as those supported by the corresponding
 * {@link EntityRefParser}.
 *
 * @param <T> The type of argument that is provided.
 * @version 1.0
 * @since 1.0
 */
public non-sealed interface SnowflakeParser<T extends @NonNull Object> 
        extends InputParser<Snowflake, T> {

    /**
     * The ID type.
     *
     * @since 1.0
     */
    enum Type {

        /**
         * An arbitrary snowflake ID. Note that this type is no different from using a
         * {@link StringParser string parser} with {@link Snowflake#of(String)}.
         */
        ANY,

        /** A user ID. */
        USER,

        /** A role ID. */
        ROLE,
        
        /** A channel ID. */
        CHANNEL,

        /** A message ID. */
        MESSAGE

    }

    /**
     * The ID type accepted.
     * 
     * <p>As no validation is performed, this only affects how the parameter is
     * registered in a slash command. It has no functional difference for the handler.
     *
     * @return The ID type.
     */
    @Pure
    Type type();
    
}
