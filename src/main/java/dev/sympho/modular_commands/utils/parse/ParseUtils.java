package dev.sympho.modular_commands.utils.parse;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.parameter.parse.ListParser;
import dev.sympho.modular_commands.api.command.parameter.parse.ParserFunction;
import dev.sympho.modular_commands.api.command.parameter.parse.Parsers;
import dev.sympho.modular_commands.api.command.parameter.parse.StringParser;
import dev.sympho.modular_commands.utils.parse.entity.ChannelParser;
import dev.sympho.modular_commands.utils.parse.entity.ChannelRefParser;
import dev.sympho.modular_commands.utils.parse.entity.EntityRef.ChannelRef;
import dev.sympho.modular_commands.utils.parse.entity.EntityRef.MessageRef;
import dev.sympho.modular_commands.utils.parse.entity.EntityRef.RoleRef;
import dev.sympho.modular_commands.utils.parse.entity.EntityRef.UserRef;
import dev.sympho.modular_commands.utils.parse.entity.MessageParser;
import dev.sympho.modular_commands.utils.parse.entity.MessageRefParser;
import dev.sympho.modular_commands.utils.parse.entity.RoleParser;
import dev.sympho.modular_commands.utils.parse.entity.RoleRefParser;
import dev.sympho.modular_commands.utils.parse.entity.UserParser;
import dev.sympho.modular_commands.utils.parse.entity.UserRefParser;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Channel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Convenience functions for common parsers.
 *
 * @version 1.0
 * @since 1.0
 */
@SuppressWarnings( "DeclarationOrder" )
public final class ParseUtils {

    /** Do not instantiate. */
    private ParseUtils() {}

    /* String to raw */

    /** Parser for booleans from strings. */
    public static final ParserFunction<String, Boolean> BOOLEAN = Parsers.functor(
            RawParser.BOOLEAN::parse );

    /** Parser for integers from strings. */
    public static final ParserFunction<String, Long> INTEGER = Parsers.functor( 
            RawParser.INTEGER::parse );

    /** Parser for floats from strings. */
    public static final ParserFunction<String, Double> FLOAT = Parsers.functor( 
            RawParser.FLOAT::parse );

    /** Parser for snowflakes from strings. */
    public static final ParserFunction<String, Snowflake> SNOWFLAKE = Parsers.functor( 
            RawParser.SNOWFLAKE::parse );

    /** Parser for messages from strings. */
    public static final MessageParser MESSAGE = new MessageParser();

    /** Parser for users from strings. */
    public static final UserParser USER = new UserParser();

    /** Parser for roles from strings. */
    public static final RoleParser ROLE = new RoleParser();

    /** Parser for message references from strings. */
    public static final MessageRefParser MESSAGE_REF = new MessageRefParser();

    /** Parser for user references from strings. */
    public static final UserRefParser USER_REF = new UserRefParser();

    /** Parser for role references from strings. */
    public static final RoleRefParser ROLE_REF = new RoleRefParser();

    /** Parser for message IDs from strings. */
    public static final ParserFunction<String, Snowflake> MESSAGE_ID = 
            MESSAGE_REF.then( Parsers.simple( MessageRef::id ) );

    /** Parser for user IDs from strings. */
    public static final ParserFunction<String, Snowflake> USER_ID = 
            USER_REF.then( Parsers.simple( UserRef::id ) );

    /** Parser for role IDs from strings. */
    public static final ParserFunction<String, Snowflake> ROLE_ID = 
            ROLE_REF.then( Parsers.simple( RoleRef::id ) );

    /** Parser for channel IDs from strings. */
    // Since it's just ID the type doesn't make a difference, so just use the bound
    public static final ParserFunction<String, Snowflake> CHANNEL_ID =
            channelRef( Channel.class ).then( Parsers.simple( ChannelRef::id ) );

    /**
     * Creates a parser for channels of the given type.
     *
     * @param <C> The channel type.
     * @param type The type.
     * @return The parser.
     */
    @SideEffectFree
    public static <C extends @NonNull Channel> ChannelParser<C> channel( 
            final Class<C> type ) {
        // Can't use a static parser due to the generics
        return new ChannelParser<>( type );
    }

    /**
     * Creates a parser for channel references of the given type.
     *
     * @param <C> The channel type.
     * @param type The type.
     * @return The parser.
     */
    @SideEffectFree
    public static <C extends @NonNull Channel> ChannelRefParser<C> channelRef( 
            final Class<C> type ) {
        // Can't use a static parser due to the generics
        return new ChannelRefParser<>( type );
    }
    
    /* Adapters */

    /**
     * Creates an adapter for an integer parser to be used with string values.
     *
     * @param <T> The parsed value type.
     * @param parser The parser to use.
     * @return The adapter parser.
     */
    @SideEffectFree
    public static <T extends @NonNull Object> ParserFunction<String, T> adaptInteger(
            final ParserFunction<Long, T> parser
    ) {
        return new StringAdapter<>( INTEGER, parser );
    }

    /**
     * Creates an adapter for an float parser to be used with string values.
     *
     * @param <T> The parsed value type.
     * @param parser The parser to use.
     * @return The adapter parser.
     */
    @SideEffectFree
    public static <T extends @NonNull Object> ParserFunction<String, T> adaptFloat(
            final ParserFunction<Double, T> parser
    ) {
        return new StringAdapter<>( FLOAT, parser );
    }

    /**
     * Creates an adapter for a snowflake parser to be used with string values.
     *
     * @param <T> The parsed value type.
     * @param parser The parser to use.
     * @return The adapter parser.
     */
    @SideEffectFree
    public static <T extends @NonNull Object> ParserFunction<String, T> adaptSnowflake(
            final ParserFunction<Snowflake, T> parser
    ) {
        return new StringAdapter<>( SNOWFLAKE, parser );
    }

    /**
     * Creates an adapter for a message parser to be used with string values.
     *
     * @param <T> The parsed value type.
     * @param parser The parser to use.
     * @return The adapter parser.
     */
    @SideEffectFree
    public static <T extends @NonNull Object> ParserFunction<String, T> adaptMessage(
            final ParserFunction<Message, T> parser
    ) {
        return new StringAdapter<>( MESSAGE, parser );
    }

    /**
     * Creates an adapter for an user parser to be used with string values.
     *
     * @param <T> The parsed value type.
     * @param parser The parser to use.
     * @return The adapter parser.
     */
    @SideEffectFree
    public static <T extends @NonNull Object> ParserFunction<String, T> adaptUser(
            final ParserFunction<User, T> parser
    ) {
        return new StringAdapter<>( USER, parser );
    }

    /**
     * Creates an adapter for a role parser to be used with string values.
     *
     * @param <T> The parsed value type.
     * @param parser The parser to use.
     * @return The adapter parser.
     */
    @SideEffectFree
    public static <T extends @NonNull Object> ParserFunction<String, T> adaptRole(
            final ParserFunction<Role, T> parser
    ) {
        return new StringAdapter<>( ROLE, parser );
    }

    /**
     * Creates an adapter for a channel parser to be used with string values.
     *
     * @param <C> The channel type.
     * @param <T> The parsed value type.
     * @param parser The parser to use.
     * @param type The channel type.
     * @return The adapter parser.
     */
    @SideEffectFree
    public static <C extends Channel, T extends @NonNull Object> ParserFunction<String, T>
            adaptChannel( final ParserFunction<C, T> parser, final Class<C> type ) {
        return new StringAdapter<>( channel( type ), parser );
    }

    /* Lists */

    /**
     * A parser that generates a flux by using the default 
     * {@link Parsers#list(ParserFunction) list parser} to split the raw
     * argument, then the given item parser to convert each item into a mono, 
     * which are then merged.
     *
     * @param <T> The item type.
     * @param parser The parser that creates monos for each item to be parsed.
     * @return The parser.
     */
    @SideEffectFree
    public static <T extends @NonNull Object> StringParser<Flux<T>> flux( 
            final ParserFunction<String, Mono<T>> parser ) {

        final var p = Parsers.list( parser );
        return ( ctx, raw ) -> p.parse( ctx, raw ).map( Flux::concat );

    }

    /**
     * A parser that parses a list of integers.
     * 
     * <p>Values are separated by whitespace.
     *
     * @return The parser.
     */
    @SideEffectFree
    public static ListParser<Long> integers() {

        return Parsers.list( INTEGER );

    }

    /**
     * A parser that parses a list of floats.
     * 
     * <p>Values are separated by whitespace.
     *
     * @return The parser.
     */
    @SideEffectFree
    public static ListParser<Double> floats() {

        return Parsers.list( FLOAT );

    }

    /**
     * A parser that parses a list of snowflake IDs.
     * 
     * <p>IDs are separated by whitespace.
     *
     * @return The parser.
     */
    @SideEffectFree
    public static ListParser<Snowflake> snowflakes() {

        return Parsers.list( SNOWFLAKE );

    }

    /**
     * A parser that parses a list of messages.
     * 
     * <p>Messages are separated by whitespace.
     *
     * @return The parser.
     */
    @SideEffectFree
    public static ListParser<Message> messages() {

        return Parsers.list( MESSAGE );

    }

    /**
     * A parser that parses a list of users.
     * 
     * <p>Users are separated by whitespace.
     *
     * @return The parser.
     */
    @SideEffectFree
    public static ListParser<User> users() {

        return Parsers.list( USER );

    }

    /**
     * A parser that parses a list of roles.
     * 
     * <p>Roles are separated by whitespace.
     *
     * @return The parser.
     */
    @SideEffectFree
    public static ListParser<Role> roles() {

        return Parsers.list( ROLE );

    }

    /**
     * A parser that parses a list of channels.
     * 
     * <p>Channels are separated by whitespace.
     *
     * @param <C> The channel type.
     * @param type The channel type.
     * @return The parser.
     */
    @SideEffectFree
    public static <C extends Channel> ListParser<C> channels( final Class<C> type ) {

        return Parsers.list( channel( type ) );

    }
    
}
