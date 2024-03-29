package dev.sympho.modular_commands.utils.parse;

import java.util.function.Function;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.context.CommandContext;
import dev.sympho.modular_commands.api.command.parameter.parse.InvalidArgumentException;
import dev.sympho.modular_commands.api.command.parameter.parse.ParserFunction;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Channel;
import reactor.core.publisher.Mono;

/**
 * Parser function that converts a string into one of the supported raw types.
 *
 * @param <R> The raw type.
 * @version 1.0
 * @since 1.0
 * @apiNote You would normally never need to manually implement this, as this interface already
 *          provides parsers for all the supported types through constants and static methods.
 */
@FunctionalInterface
public interface RawParser<R extends @NonNull Object> extends Function<String, Mono<R>> {

    /**
     * Parser for strings.
     *
     * @apiNote This is a trivial parser, but it exists for consistency reasons.
     */
    RawParser<String> STRING = Mono::just;

    /** Parser for booleans. */
    @SuppressWarnings( "switch.expression" ) // Weird issue with the interning checker
    RawParser<Boolean> BOOLEAN = raw -> switch ( raw.toLowerCase() ) {
        case "true" -> Mono.just( true );
        case "false" -> Mono.just( false );
        default -> Mono.error( new InvalidArgumentException(
            "Not a valid boolean: " + raw
        ) );
    };

    /** Parser for integers. */
    RawParser<Long> INTEGER = raw -> {
        try {
            return Mono.just( Long.decode( raw ) );
        } catch ( final NumberFormatException ex ) {
            return Mono.error( new InvalidArgumentException( 
                "Not a valid integer: " + raw, ex
             ) );
        }
    };

    /** Parser for floats. */
    RawParser<Double> FLOAT = raw -> {
        try {
            return Mono.just( Double.parseDouble( raw ) );
        } catch ( final NumberFormatException ex ) {
            return Mono.error( new InvalidArgumentException( 
                "Not a valid number: " + raw, ex
            ) );
        }
    };

    /** Parser for snowflakes. */
    RawParser<Snowflake> SNOWFLAKE = raw -> {
        try {
            return Mono.just( Snowflake.of( raw ) );
        } catch ( final NumberFormatException ex ) {
            return Mono.error( new InvalidArgumentException( 
                "Not a valid snowflake: " + raw, ex
             ) );
        }
    };

    /**
     * Creates a raw parser that deletegates to the given parser function with the given context.
     *
     * @param <R> The raw value type.
     * @param context The execution context.
     * @param parser The parser to delegate to.
     * @return The created parser.
     */
    private static <R extends @NonNull Object> RawParser<R> from( 
            final CommandContext context, final ParserFunction<String, R> parser ) {

        return raw -> {

            try {
                return parser.parse( context, raw );
            } catch ( final InvalidArgumentException ex ) {
                return Mono.error( ex );
            }

        };

    }

    /**
     * Creates a parser for messages with the given context.
     *
     * @param context The context to use.
     * @return The parser.
     */
    @SideEffectFree
    static RawParser<Message> message( final CommandContext context ) {
        return from( context, ParseUtils.MESSAGE );
    }

    /**
     * Creates a parser for users with the given context.
     *
     * @param context The context to use.
     * @return The parser.
     */
    @SideEffectFree
    static RawParser<User> user( final CommandContext context ) {
        return from( context, ParseUtils.USER );
    }

    /**
     * Creates a parser for roles with the given context.
     *
     * @param context The context to use.
     * @return The parser.
     */
    @SideEffectFree
    static RawParser<Role> role( final CommandContext context ) {
        return from( context, ParseUtils.ROLE );
    }

    /**
     * Creates a parser for channels with the given context.
     *
     * @param <C> The channel type.
     * @param context The context to use.
     * @param type The channel type.
     * @return The parser.
     */
    @SideEffectFree
    static <C extends @NonNull Channel> RawParser<C> channel( 
            final CommandContext context, final Class<C> type ) {
        // Can't use a static parser due to the generics
        return from( context, ParseUtils.channel( type ) );
    }

    /**
     * Creates a parser for message IDs with the given context.
     *
     * @param context The context to use.
     * @return The parser.
     */
    @SideEffectFree
    static RawParser<Snowflake> messageId( final CommandContext context ) {
        return from( context, ParseUtils.MESSAGE_ID );
    }

    /**
     * Creates a parser for user IDs with the given context.
     *
     * @param context The context to use.
     * @return The parser.
     */
    @SideEffectFree
    static RawParser<Snowflake> userId( final CommandContext context ) {
        return from( context, ParseUtils.USER_ID );
    }

    /**
     * Creates a parser for role IDs with the given context.
     *
     * @param context The context to use.
     * @return The parser.
     */
    @SideEffectFree
    static RawParser<Snowflake> roleId( final CommandContext context ) {
        return from( context, ParseUtils.ROLE_ID );
    }

    /**
     * Creates a parser for channel IDs with the given context.
     *
     * @param context The context to use.
     * @return The parser.
     */
    @SideEffectFree
    static RawParser<Snowflake> channelId( final CommandContext context ) {
        return from( context, ParseUtils.CHANNEL_ID );
    }

    /**
     * Parses the given string into a raw value.
     *
     * @param raw The original string.
     * @return The pre-parsed raw value. May fail with a {@link InvalidArgumentException}
     *         if the received value is invalid.
     */
    @SideEffectFree
    Mono<R> parse( String raw );

    @Override
    @SideEffectFree
    default Mono<R> apply( final String raw ) {
        return parse( raw );
    }
    
}
