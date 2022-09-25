package dev.sympho.modular_commands.api.command.parameter.parse;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.nullness.util.NullnessUtil;
import org.checkerframework.common.value.qual.IntRange;

import dev.sympho.modular_commands.api.command.context.CommandContext;
import dev.sympho.modular_commands.api.command.parameter.parse.ChoicesParser.Choice;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Channel;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufMono;
import reactor.netty.http.client.HttpClientResponse;

/**
 * Convenience functions for defining argument parsers.
 *
 * @version 1.0
 * @since 1.0
 */
public final class Parsers {

    /** Do not instantiate. */
    private Parsers() {}

    /**
     * Returns the raw value received.
     *
     * @param <R> The raw type.
     * @param context The context.
     * @param raw The raw value.
     * @return A mono that issues the raw value.
     */
    private static <R extends @NonNull Object> Mono<R> raw( 
            final CommandContext context, final R raw ) {
        return Mono.just( raw );
    }

    /**
     * Validates that the given range is valid.
     *
     * @param <P> The raw type.
     * @param minimum The minimum value (inclusive).
     * @param maximum The maximum value (inclusive).
     * @throws IllegalArgumentException if the range is invalid.
     */
    private static <P extends @NonNull Comparable<? super P>> void validateRange( 
            final P minimum, final P maximum ) throws IllegalArgumentException {

        if ( minimum.compareTo( maximum ) > 0 ) {
            throw new IllegalArgumentException( "Invalid range [%s, %s]".formatted( 
                    minimum, maximum ) );
        }

    }

    /* Adapters for other function signatures */

    /**
     * Uses a parser that does not depend on the execution context.
     *
     * @param <R> The raw type received.
     * @param <T> The parsed argument type.
     * @param parser The parser to use.
     * @return The adapted parser.
     */
    public static <R extends @NonNull Object, T extends @NonNull Object> 
            ParserFunction<R, T> global( final Function<R, Mono<T>> parser ) {
        return ( ctx, raw ) -> parser.apply( raw );
    }

    /**
     * Uses a parser that executes synchronously.
     *
     * @param <R> The raw type received.
     * @param <T> The parsed argument type.
     * @param parser The parser to use.
     * @return The adapted parser.
     */
    public static <R extends @NonNull Object, T extends @NonNull Object> 
            ParserFunction<R, T> sync( final BiFunction<CommandContext, R, T> parser ) {
        return ( ctx, raw ) -> Mono.just( parser.apply( ctx, raw ) );
    }

    /**
     * Uses a parser that executes synchronously and does not depend on the 
     * execution context.
     *
     * @param <R> The raw type received.
     * @param <T> The parsed argument type.
     * @param parser The parser to use.
     * @return The adapted parser.
     */
    public static <R extends @NonNull Object, T extends @NonNull Object> 
            ParserFunction<R, T> sync( final Function<R, T> parser ) {
        return global( parser.andThen( Mono::just ) );
    }

    /* Choices utils */

    /**
     * Creates a parser out of a set of choices.
     *
     * @param <P> The raw argument type.
     * @param <T> The parsed type.
     * @param choices The choice mappings.
     * @return The parser.
     */
    private static <P extends @NonNull Object, T extends @NonNull Object> 
            ParserFunction<P, T> choiceParser( final List<Map.Entry<Choice<P>, T>> choices ) {

        final Map<P, T> mapping = choices.stream()
                .collect( Collectors.toMap( e -> e.getKey().value(), Map.Entry::getValue ) );

        // System guarantees it will be a valid choice by this point
        return sync( choice -> NullnessUtil.castNonNull( mapping.get( choice ) ) );

    }

    /**
     * Converts a list of choice mappings into just the choices.
     *
     * @param <P> The raw argument type.
     * @param choices The choice mappings.
     * @return The choices.
     */
    private static <P extends @NonNull Object> List<Choice<P>> choiceEntries( 
            final List<? extends Map.Entry<Choice<P>, ?>> choices ) {

        return choices.stream()
                .map( Map.Entry::getKey )
                .toList();

    }

    /* Integers */

    /**
     * Creates a parser that receives plain integer values.
     *
     * @return The parser.
     */
    public static IntegerParser<Long> integer() {
        return integer( Parsers::raw );
    }

    /**
     * Creates a parser that uses the given function to parse received values.
     *
     * @param <T> The parsed argument type.
     * @param parser The parser to use.
     * @return The parser.
     */
    public static <T extends @NonNull Object> IntegerParser<T> integer( 
            final ParserFunction<Long, T> parser ) {

        return new IntegerParserImpl<>( null, null, null, parser );

    }

    /**
     * Creates a parser that receives plain integer values from within a set of choices.
     *
     * @param choices The allowed values.
     * @return The parser.
     */
    public static IntegerParser<Long> integer( final List<Choice<Long>> choices ) {

        return new IntegerParserImpl<>( 
                choices, 
                null, null, 
                Parsers::raw
        );

    }

    /**
     * Creates a parser that receives a value from within a set of choices.
     *
     * @param <T> The parsed argument type.
     * @param choices The choices.
     * @return The parser.
     */
    public static <T extends @NonNull Object> IntegerParser<T> choiceInteger( 
            final List<Map.Entry<Choice<Long>, T>> choices ) {

        return new IntegerParserImpl<>( 
                choiceEntries( choices ), 
                null, null, 
                choiceParser( choices ) 
        );

    }

    /**
     * Creates a parser that receives plain integer values, which must be at least
     * the given value.
     *
     * @param minimum The minimum value allowed (inclusive).
     * @return The parser.
     */
    public static IntegerParser<Long> integerAbove( final long minimum ) {
        return integerAbove( minimum, Parsers::raw );
    }

    /**
     * Creates a parser that uses the given function to parse received values, which must be at 
     * least the given value.
     *
     * @param <T> The parsed argument type.
     * @param minimum The minimum value allowed (inclusive).
     * @param parser The parser to use.
     * @return The parser.
     */
    public static <T extends @NonNull Object> IntegerParser<T> integerAbove( final long minimum, 
            final ParserFunction<Long, T> parser ) {

        return new IntegerParserImpl<>( null, minimum, null, parser );

    }

    /**
     * Creates a parser that receives plain integer values, which must be at most
     * the given value.
     *
     * @param maximum The maximum value allowed (inclusive).
     * @return The parser.
     */
    public static IntegerParser<Long> integerBelow( final long maximum ) {
        return integerBelow( maximum, Parsers::raw );
    }

    /**
     * Creates a parser that uses the given function to parse received values, which must be at 
     * most the given value.
     *
     * @param <T> The parsed argument type.
     * @param maximum The maximum value allowed (inclusive).
     * @param parser The parser to use.
     * @return The parser.
     */
    public static <T extends @NonNull Object> IntegerParser<T> integerBelow( final long maximum, 
            final ParserFunction<Long, T> parser ) {

        return new IntegerParserImpl<>( null, null, maximum, parser );

    }

    /**
     * Creates a parser that receives plain integer values, which must be between
     * the given values.
     *
     * @param minimum The minimum value allowed (inclusive).
     * @param maximum The maximum value allowed (inclusive).
     * @return The parser.
     */
    public static IntegerParser<Long> integerBetween( final long minimum, final long maximum ) {
        return integerBetween( minimum, maximum, Parsers::raw );
    }

    /**
     * Creates a parser that uses the given function to parse received values, which must be
     * between the given values.
     *
     * @param <T> The parsed argument type.
     * @param minimum The minimum value allowed (inclusive).
     * @param maximum The maximum value allowed (inclusive).
     * @param parser The parser to use.
     * @return The parser.
     */
    public static <T extends @NonNull Object> IntegerParser<T> integerBetween( 
            final long minimum, final long maximum, 
            final ParserFunction<Long, T> parser ) {

        validateRange( minimum, maximum );
        return new IntegerParserImpl<>( null, minimum, maximum, parser );

    }

    /* Floats */
    /* Sidenote: can't call it "float" because that's a keyword so called it "number instead" */

    /**
     * Creates a parser that receives plain floating-point values.
     *
     * @return The parser.
     */
    public static FloatParser<Double> number() {
        return number( Parsers::raw );
    }

    /**
     * Creates a parser that uses the given function to parse received values.
     *
     * @param <T> The parsed argument type.
     * @param parser The parser to use.
     * @return The parser.
     */
    public static <T extends @NonNull Object> FloatParser<T> number( 
            final ParserFunction<Double, T> parser ) {

        return new FloatParserImpl<>( null, null, null, parser );
        
    }

    /**
     * Creates a parser that receives plain floating-point values from within a set of choices.
     *
     * @param choices The allowed values.
     * @return The parser.
     */
    public static FloatParser<Double> number( final List<Choice<Double>> choices ) {

        return new FloatParserImpl<>( 
                choices, 
                null, null, 
                Parsers::raw
        );

    }

    /**
     * Creates a parser that receives a value from within a set of choices.
     *
     * @param <T> The parsed argument type.
     * @param choices The choices.
     * @return The parser.
     */
    public static <T extends @NonNull Object> FloatParser<T> numberChoice( 
            final List<Map.Entry<Choice<Double>, T>> choices ) {

        return new FloatParserImpl<>( 
                choiceEntries( choices ), 
                null, null, 
                choiceParser( choices ) 
        );

    }

    /**
     * Creates a parser that receives plain floating-point values, which must be at least
     * the given value.
     *
     * @param minimum The minimum value allowed (inclusive).
     * @return The parser.
     */
    public static FloatParser<Double> numberAbove( final double minimum ) {
        return numberAbove( minimum, Parsers::raw );
    }

    /**
     * Creates a parser that uses the given function to parse received values, which must be at 
     * least the given value.
     *
     * @param <T> The parsed argument type.
     * @param minimum The minimum value allowed (inclusive).
     * @param parser The parser to use.
     * @return The parser.
     */
    public static <T extends @NonNull Object> FloatParser<T> numberAbove( final double minimum, 
            final ParserFunction<Double, T> parser ) {

        return new FloatParserImpl<>( null, minimum, null, parser );

    }

    /**
     * Creates a parser that receives plain floating-point values, which must be at most
     * the given value.
     *
     * @param maximum The maximum value allowed (inclusive).
     * @return The parser.
     */
    public static FloatParser<Double> numberBelow( final double maximum ) {
        return numberBelow( maximum, Parsers::raw );
    }

    /**
     * Creates a parser that uses the given function to parse received values, which must be at 
     * most the given value.
     *
     * @param <T> The parsed argument type.
     * @param maximum The maximum value allowed (inclusive).
     * @param parser The parser to use.
     * @return The parser.
     */
    public static <T extends @NonNull Object> FloatParser<T> numberBelow( final double maximum, 
            final ParserFunction<Double, T> parser ) {

        return new FloatParserImpl<>( null, null, maximum, parser );

    }

    /**
     * Creates a parser that receives plain floating-point values, which must be between
     * the given values.
     *
     * @param minimum The minimum value allowed (inclusive).
     * @param maximum The maximum value allowed (inclusive).
     * @return The parser.
     */
    public static FloatParser<Double> numberBetween( final double minimum, final double maximum ) {
        return numberBetween( minimum, maximum, Parsers::raw );
    }

    /**
     * Creates a parser that uses the given function to parse received values, which must be
     * between the given values.
     *
     * @param <T> The parsed argument type.
     * @param minimum The minimum value allowed (inclusive).
     * @param maximum The maximum value allowed (inclusive).
     * @param parser The parser to use.
     * @return The parser.
     */
    public static <T extends @NonNull Object> FloatParser<T> numberBetween( 
            final double minimum, final double maximum, 
            final ParserFunction<Double, T> parser ) {

        validateRange( minimum, maximum );
        return new FloatParserImpl<>( null, minimum, maximum, parser );

    }

    /* Strings */

    /**
     * Creates a parser that receives plain string values.
     *
     * @return The parser.
     */
    public static StringParser<String> string() {
        return string( Parsers::raw );
    }

    /**
     * Creates a parser that uses the given function to parse received values.
     *
     * @param <T> The parsed argument type.
     * @param parser The parser to use.
     * @return The parser.
     */
    public static <T extends @NonNull Object> StringParser<T> string( 
            final ParserFunction<String, T> parser ) {

        return new StringParserImpl<>( null, parser );
        
    }

    /**
     * Creates a parser that receives plain string values from within a set of choices.
     *
     * @param choices The allowed values.
     * @return The parser.
     */
    public static StringParser<String> string( final List<Choice<String>> choices ) {

        return new StringParserImpl<>( 
                choices,
                Parsers::raw
        );

    }

    /**
     * Creates a parser that receives a value from within a set of choices.
     *
     * @param <T> The parsed argument type.
     * @param choices The choices.
     * @return The parser.
     */
    public static <T extends @NonNull Object> StringParser<T> stringChoice( 
            final List<Map.Entry<Choice<String>, T>> choices ) {

        return new StringParserImpl<>( 
                choiceEntries( choices ), 
                choiceParser( choices ) 
        );

    }

    /* Attachments */

    /**
     * Creates a parser that receives raw attachment values.
     *
     * @return The parser.
     */
    public static AttachmentParser<Attachment> attachment() {
        return attachment( Parsers::raw );
    }

    /**
     * Creates a parser that uses the given function to parse received values.
     *
     * @param <T> The parsed argument type.
     * @param parser The parser to use.
     * @return The parser.
     */
    public static <T extends @NonNull Object> AttachmentParser<T> attachment(
            final ParserFunction<Attachment, T> parser
    ) {

        return new AttachmentParserImpl<>( parser );

    }

    /**
     * Creates a parser that uses the given validator and parser to parse the contents
     * received in an attachment.
     *
     * @param <T> The parsed argument type.
     * @param validator The validator to use.
     * @param maxSize The maximum file size allowed.
     * @param parser The parser to use.
     * @return The parser.
     * @throws IllegalArgumentException if the size is negative.
     * @apiNote Validation is applied before fetching the attachment data.
     */
    public static <T extends @NonNull Object> AttachmentDataParser<T> attachment(
            final AttachmentParserStages.Validator validator,
            final @IntRange( from = 0 ) int maxSize,
            final AttachmentParserStages.Parser<T> parser
    ) throws IllegalArgumentException {

        if ( maxSize < 0 ) {
            throw new IllegalArgumentException( "Max size must be non-negative" );
        }

        return new AttachmentDataParserImpl<>( 
                Objects.requireNonNull( validator ), 
                maxSize, 
                Objects.requireNonNull( parser )
        );

    }

    /**
     * Creates a parser that uses the given parser to parse the contents
     * received in an attachment, with no validation performed prior to
     * receiving attachment data (other than the size limit).
     *
     * @param <T> The parsed argument type.
     * @param maxSize The maximum file size allowed.
     * @param parser The parser to use.
     * @return The parser.
     */
    public static <T extends @NonNull Object> AttachmentDataParser<T> attachment(
            final @IntRange( from = 0 ) int maxSize,
            final AttachmentParserStages.Parser<T> parser
    ) {

        return attachment( attachment -> {}, maxSize, parser );

    }

    /**
     * Creates a parser that uses the given parser to parse the contents
     * received in an attachment, with no validation performed prior to
     * receiving attachment data, and with unbounded file size.
     *
     * @param <T> The parsed argument type.
     * @param parser The parser to use.
     * @return The parser.
     */
    public static <T extends @NonNull Object> AttachmentDataParser<T> attachment(
            final AttachmentParserStages.Parser<T> parser
    ) {

        return attachment( Integer.MAX_VALUE, parser );

    }

    /**
     * Creates a parser that uses the given parser to parse the contents
     * received in a text file.
     *
     * @param <T> The parsed argument type.
     * @param maxSize The maximum file size allowed.
     * @param parser The parser to use.
     * @return The parser.
     */
    public static <T extends @NonNull Object> TextFileParser<T> textFile(
            final @IntRange( from = 0 ) int maxSize,
            final ParserFunction<String, T> parser
    ) {

        return new TextFileParserImpl<>( maxSize, parser );

    }

    /**
     * Creates a parser that uses the given parser to parse the contents
     * received in a text file, with unbounded file size.
     *
     * @param <T> The parsed argument type.
     * @param parser The parser to use.
     * @return The parser.
     */
    public static <T extends @NonNull Object> TextFileParser<T> textFile(
            final ParserFunction<String, T> parser
    ) {

        return textFile( Integer.MAX_VALUE, parser );

    }

    /**
     * Creates a parser that receives the plain contents of a text file.
     *
     * @param maxSize The maximum file size allowed.
     * @return The parser.
     */
    public static TextFileParser<String> textFile( final @IntRange( from = 0 ) int maxSize ) {

        return textFile( maxSize, Parsers::raw );

    }

    /**
     * Creates a parser that receives the plain contents of a text file, with unbounded file size.
     *
     * @return The parser.
     */
    public static TextFileParser<String> textFile() {

        return textFile( Integer.MAX_VALUE );

    }

    /* Snowflake */

    /**
     * Creates a parser that uses the given function to parse received values.
     * 
     * <p>Note that validation is performed other than that the value is a properly
     * formatted snowflake ID, and for interaction commands it will appear as a
     * string parameter.
     *
     * @param <T> The parsed argument type.
     * @param parser The parser to use.
     * @return The parser.
     */
    public static <T extends @NonNull Object> SnowflakeParser<T> snowflake( 
            final ParserFunction<Snowflake, T> parser
    ) {

        return new SnowflakeParserImpl<>( SnowflakeParser.Type.ANY, parser );

    }

    /**
     * Creates a parser that receives snowflake IDs.
     * 
     * <p>Note that validation is performed other than that the value is a properly
     * formatted snowflake ID, and for interaction commands it will appear as a
     * string parameter. That makes it functionally identical to {@link #string(ParserFunction)}
     * with a parser function of {@link Snowflake#of(String)}.
     *
     * @return The parser.
     */
    public static SnowflakeParser<Snowflake> snowflake() {

        return snowflake( Parsers::raw );

    }

    /**
     * Creates a parser that uses the given function to parse received values.
     * 
     * <p>Note that this parser does not itself perform any validation on whether the snowflake
     * received is a valid user ID. It is useful only for hybrid commands (text and interaction)
     * to have a user-type argument (which is fully received with the interaction) without making
     * the text case fetch the entire user (which is an additional request in text commands)
     * when just the ID is necessary.
     * 
     * <p>If validation is necessary and text-command performance is not a concern use
     * {@link #user()}.
     *
     * @param <T> The parsed argument type.
     * @param parser The parser to use.
     * @return The parser.
     */
    public static <T extends @NonNull Object> SnowflakeParser<T> userId( 
            final ParserFunction<Snowflake, T> parser
    ) {

        return new SnowflakeParserImpl<>( SnowflakeParser.Type.USER, parser );

    }

    /**
     * Creates a parser that receives user IDs.
     * 
     * <p>Note that this parser does not itself perform any validation on whether the snowflake
     * received is a valid user ID. It is useful only for hybrid commands (text and interaction)
     * to have a user-type argument (which is fully received with the interaction) without making
     * the text case fetch the entire user (which is an additional request in text commands)
     * when just the ID is necessary.
     * 
     * <p>If validation is necessary and text-command performance is not a concern use
     * {@link #user()}.
     *
     * @return The parser.
     */
    public static SnowflakeParser<Snowflake> userId() {

        return userId( Parsers::raw );

    }

    /**
     * Creates a parser that uses the given function to parse received values.
     * 
     * <p>Note that this parser does not itself perform any validation on whether the snowflake
     * received is a valid role ID. It is useful only for hybrid commands (text and interaction)
     * to have a role-type argument (which is fully received with the interaction) without making
     * the text case fetch the entire role (which is an additional request in text commands)
     * when just the ID is necessary.
     * 
     * <p>If validation is necessary and text-command performance is not a concern use
     * {@link #role()}.
     *
     * @param <T> The parsed argument type.
     * @param parser The parser to use.
     * @return The parser.
     */
    public static <T extends @NonNull Object> SnowflakeParser<T> roleId( 
            final ParserFunction<Snowflake, T> parser
    ) {

        return new SnowflakeParserImpl<>( SnowflakeParser.Type.ROLE, parser );

    }

    /**
     * Creates a parser that receives role IDs.
     * 
     * <p>Note that this parser does not itself perform any validation on whether the snowflake
     * received is a valid role ID. It is useful only for hybrid commands (text and interaction)
     * to have a role-type argument (which is fully received with the interaction) without making
     * the text case fetch the entire role (which is an additional request in text commands)
     * when just the ID is necessary.
     * 
     * <p>If validation is necessary and text-command performance is not a concern use
     * {@link #role()}.
     *
     * @return The parser.
     */
    public static SnowflakeParser<Snowflake> roleId() {

        return roleId( Parsers::raw );

    }

    /**
     * Creates a parser that uses the given function to parse received values.
     * 
     * <p>Note that this parser does not itself perform any validation on whether the snowflake
     * received is a valid channel ID. It is useful only for hybrid commands (text and interaction)
     * to have a channel-type argument (which is fully received with the interaction) without making
     * the text case fetch the entire channel (which is an additional request in text commands)
     * when just the ID is necessary.
     * 
     * <p>If validation is necessary and text-command performance is not a concern use
     * {@link #channel(Class)}.
     *
     * @param <T> The parsed argument type.
     * @param parser The parser to use.
     * @return The parser.
     */
    public static <T extends @NonNull Object> SnowflakeParser<T> channelId( 
            final ParserFunction<Snowflake, T> parser
    ) {

        return new SnowflakeParserImpl<>( SnowflakeParser.Type.CHANNEL, parser );

    }

    /**
     * Creates a parser that receives channel IDs.
     * 
     * <p>Note that this parser does not itself perform any validation on whether the snowflake
     * received is a valid channel ID. It is useful only for hybrid commands (text and interaction)
     * to have a channel-type argument (which is fully received with the interaction) without making
     * the text case fetch the entire channel (which is an additional request in text commands)
     * when just the ID is necessary.
     * 
     * <p>If validation is necessary and text-command performance is not a concern use
     * {@link #channel(Class)}.
     *
     * @return The parser.
     */
    public static SnowflakeParser<Snowflake> channelId() {

        return channelId( Parsers::raw );

    }

    /* Entities */

    /**
     * Creates a parser that receives raw user values.
     *
     * @return The parser.
     */
    public static UserArgumentParser<User> user() {

        return user( Parsers::raw );

    }

    /**
     * Creates a parser that uses the given function to parse received values.
     *
     * @param <T> The parsed argument type.
     * @param parser The parser to use.
     * @return The parser.
     */
    public static <T extends @NonNull Object> UserArgumentParser<T> user(
            final ParserFunction<User, T> parser
    ) {

        return parser::parse;

    }

    /**
     * Creates a parser that receives raw role values.
     *
     * @return The parser.
     */
    public static RoleArgumentParser<Role> role() {

        return role( Parsers::raw );

    }

    /**
     * Creates a parser that uses the given function to parse received values.
     *
     * @param <T> The parsed argument type.
     * @param parser The parser to use.
     * @return The parser.
     */
    public static <T extends @NonNull Object> RoleArgumentParser<T> role(
            final ParserFunction<Role, T> parser
    ) {

        return parser::parse;

    }

    /**
     * Creates a parser that receives raw message values.
     *
     * @return The parser.
     */
    public static MessageArgumentParser<Message> message() {

        return message( Parsers::raw );

    }

    /**
     * Creates a parser that uses the given function to parse received values.
     *
     * @param <T> The parsed argument type.
     * @param parser The parser to use.
     * @return The parser.
     */
    public static <T extends @NonNull Object> MessageArgumentParser<T> message(
            final ParserFunction<Message, T> parser
    ) {

        return parser::parse;

    }

    /**
     * Creates a parser that receives raw channel values.
     *
     * @param <C> The channel type.
     * @param type The channel type.
     * @return The parser.
     */
    public static <C extends @NonNull Channel> ChannelArgumentParser<C, C> channel( 
            final Class<C> type ) {

        return channel( type, Parsers::raw );

    }

    /**
     * Creates a parser that uses the given function to parse received values.
     *
     * @param <C> The channel type.
     * @param <T> The parsed argument type.
     * @param type The channel type.
     * @param parser The parser to use.
     * @return The parser.
     */
    public static <C extends @NonNull Channel, T extends @NonNull Object> 
            ChannelArgumentParser<C, T> channel( final Class<C> type,
            final ParserFunction<C, T> parser
    ) {

        return new ChannelParserImpl<>( type, parser );

    }

    /* Implementation classes */

    /**
     * A parser for integer values.
     *
     * @param <T> The argument type.
     * @param choices The allowed values.
     * @param minimum The minimum raw value allowed.
     * @param maximum The maximum raw value allowed.
     * @param parser The function to use to parse values.
     * @since 1.0
     */
    private record IntegerParserImpl<T extends @NonNull Object>(
            @Nullable List<Choice<Long>> choices,
            @Nullable Long minimum, @Nullable Long maximum,
            ParserFunction<Long, T> parser
    ) implements IntegerParser<T> {

        @Override
        public Mono<T> parse( final CommandContext context, final Long raw ) 
                throws InvalidArgumentException {

            return parser.parse( context, raw );

        }

    }

    /**
     * A parser for floating-point values.
     *
     * @param <T> The argument type.
     * @param choices The allowed values.
     * @param minimum The minimum raw value allowed.
     * @param maximum The maximum raw value allowed.
     * @param parser The function to use to parse values.
     * @since 1.0
     */
    private record FloatParserImpl<T extends @NonNull Object>(
            @Nullable List<Choice<Double>> choices,
            @Nullable Double minimum, @Nullable Double maximum,
            ParserFunction<Double, T> parser
    ) implements FloatParser<T> {

        @Override
        public Mono<T> parse( final CommandContext context, final Double raw ) 
                throws InvalidArgumentException {

            return parser.parse( context, raw );

        }

    }

    /**
     * A parser for floating-point values.
     *
     * @param <T> The argument type.
     * @param choices The allowed values.
     * @param parser The function to use to parse values.
     * @since 1.0
     */
    private record StringParserImpl<T extends @NonNull Object>(
            @Nullable List<Choice<String>> choices,
            ParserFunction<String, T> parser
    ) implements StringParser<T> {

        @Override
        public Mono<T> parse( final CommandContext context, final String raw ) 
                throws InvalidArgumentException {

            return parser.parse( context, raw );

        }

    }

    /**
     * A parser for attachment values.
     *
     * @param <T> The argument type.
     * @param parser The function to use to parse values.
     * @since 1.0
     */
    private record AttachmentParserImpl<T extends @NonNull Object>(
            ParserFunction<Attachment, T> parser
    ) implements AttachmentParser<T> {

        @Override
        public Mono<T> parse( final CommandContext context, final Attachment raw ) 
                throws InvalidArgumentException {

            return parser.parse( context, raw );

        }

    }

    /**
     * A parser for attachment data values.
     *
     * @param <T> The argument type.
     * @param validator The validator to use.
     * @param maxSize The maximum file size allowed.
     * @param parser The parser to use.
     * @since 1.0
     */
    private record AttachmentDataParserImpl<T extends @NonNull Object>(
            AttachmentParserStages.Validator validator,
            @IntRange( from = 0 ) int maxSize,
            AttachmentParserStages.Parser<T> parser
    ) implements AttachmentDataParser<T> {

        @Override
        public void validate( final Attachment attachment ) throws InvalidArgumentException {

            validator.validate( attachment );

        }

        @Override
        public @IntRange( from = 0 ) int maxSize() {

            return maxSize;

        }

        @Override
        public Mono<T> parse( final CommandContext context, final HttpClientResponse response,
                final ByteBufMono body ) throws InvalidArgumentException {

            return parser.parse( context, response, body );

        }

    }

    /**
     * A parser for text file attachment values.
     *
     * @param <T> The argument type.
     * @param maxSize The maximum file size allowed.
     * @param parser The function to use to parse values.
     * @since 1.0
     */
    private record TextFileParserImpl<T extends @NonNull Object>(
            @IntRange( from = 0 ) int maxSize,
            ParserFunction<String, T> parser
    ) implements TextFileParser<T> {

        @Override
        public @IntRange( from = 0 ) int maxSize() {

            return maxSize;

        }

        @Override
        public Mono<T> parse( final CommandContext context, final String content ) 
                throws InvalidArgumentException {

            return parser.parse( context, content );

        }

    }

    /**
     * A parser for snowflake values.
     *
     * @param <T> The argument type.
     * @param type The ID type.
     * @param parser The function to use to parse values.
     * @since 1.0
     */
    private record SnowflakeParserImpl<T extends @NonNull Object>(
            SnowflakeParser.Type type,
            ParserFunction<Snowflake, T> parser
    ) implements SnowflakeParser<T> {

        @Override
        public Mono<T> parse( final CommandContext context, final Snowflake raw )
                throws InvalidArgumentException {

            return parser.parse( context, raw );

        }

    }

    /**
     * A parser for channel values.
     *
     * @param <C> The channel type.
     * @param <T> The argument type.
     * @param type The channel type
     * @param parser The function to use to parse values.
     * @since 1.0
     */
    private record ChannelParserImpl<C extends Channel, T extends @NonNull Object>(
            Class<C> type,
            ParserFunction<C, T> parser
    ) implements ChannelArgumentParser<C, T> {

        @Override
        public Mono<T> parse( final CommandContext context, final C raw )
                throws InvalidArgumentException {

            return parser.parse( context, raw );

        }

    }
    
}
