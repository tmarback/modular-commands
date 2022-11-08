package dev.sympho.modular_commands.utils.parse;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.parameter.parse.ListParser;
import dev.sympho.modular_commands.api.command.parameter.parse.ParserFunction;
import dev.sympho.modular_commands.api.command.parameter.parse.Parsers;
import dev.sympho.modular_commands.api.command.parameter.parse.StringParser;
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
public final class ParseUtils {

    /** Do not instantiate. */
    private ParseUtils() {}

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
     * A parser that parses a list of snowflake IDs.
     * 
     * <p>IDs are separated by whitespace.
     *
     * @return The parser.
     */
    @SideEffectFree
    public static ListParser<Snowflake> snowflakes() {

        return Parsers.list( Parsers.simple( Snowflake::of ) );

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

        return Parsers.list( new UserParser() );

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

        return Parsers.list( new RoleParser() );

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

        return Parsers.list( new ChannelParser<>( type ) );

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

        return Parsers.list( new MessageParser() );

    }
    
}
