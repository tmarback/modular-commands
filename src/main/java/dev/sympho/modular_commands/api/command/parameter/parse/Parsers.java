package dev.sympho.modular_commands.api.command.parameter.parse;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.nullness.util.NullnessUtil;
import org.checkerframework.common.value.qual.IntRange;
import org.checkerframework.common.value.qual.MinLen;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

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
     * @apiNote This method may be used as a {@link ParserFunction} using a method reference.
     */
    @SideEffectFree
    public static <R extends @NonNull Object> Mono<R> raw( 
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
    @SideEffectFree
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
     * @param parser The parserto use.
     * @return The parser.
     * @apiNote This is a convenience for adapting functions that do not quite 
     *          match the interface.
     */
    @Pure
    public static <R extends @NonNull Object, T extends @NonNull Object> 
            ParserFunction<R, T> functor( final Functor<R, T> parser ) {
        return parser;
    }

    /**
     * Uses a parser that executes synchronously.
     *
     * @param <R> The raw type received.
     * @param <T> The parsed argument type.
     * @param parser The parser to use.
     * @return The parser.
     * @apiNote This is a convenience for adapting functions that do not quite 
     *          match the interface.
     */
    @Pure
    public static <R extends @NonNull Object, T extends @NonNull Object> 
            ParserFunction<R, T> sync( final Synchronous<R, T> parser ) {
        return parser;
    }

    /**
     * Uses a parser that executes synchronously and does not depend on the 
     * execution context.
     *
     * @param <R> The raw type received.
     * @param <T> The parsed argument type.
     * @param parser The parser to use.
     * @return The parser.
     * @apiNote This is a convenience for adapting functions that do not quite 
     *          match the interface.
     */
    @Pure
    public static <R extends @NonNull Object, T extends @NonNull Object> 
            ParserFunction<R, T> simple( final Simple<R, T> parser ) {
        return parser;
    }

    /* Choices utils */

    /**
     * Creates a parser out of a set of choices.
     *
     * @param <P> The raw argument type.
     * @param <T> The parsed type.
     * @param choices The choice mappings.
     * @return The parser.
     * @throws IllegalArgumentException if the choice list is empty.
     */
    @SideEffectFree
    private static <P extends @NonNull Object, T extends @NonNull Object> 
            ParserFunction<P, T> choiceParser( 
                final @MinLen( 1 ) List<Entry<Choice<P>, T>> choices )
                throws IllegalArgumentException {

        if ( choices.isEmpty() ) {
            throw new IllegalArgumentException( "There must be at least one choice." );
        }

        final Map<P, T> mapping = choices.stream()
                .collect( Collectors.toMap( e -> e.getKey().value(), Entry::getValue ) );

        // System guarantees it will be a valid choice by this point
        return simple( choice -> NullnessUtil.castNonNull( mapping.get( choice ) ) );

    }

    /**
     * Converts a list of choice mappings into just the choices.
     *
     * @param <P> The raw argument type.
     * @param choices The choice mappings.
     * @return The choices.
     */
    @SideEffectFree
    private static <P extends @NonNull Object> List<Choice<P>> choiceEntries( 
            final List<? extends Entry<Choice<P>, ?>> choices ) {

        return choices.stream()
                .map( Entry::getKey )
                .toList();

    }

    /* Booleans */

    /**
     * Creates a parser that receives plain boolean values.
     *
     * @return The parser.
     */
    @SideEffectFree
    public static BooleanParser<Boolean> bool() {
        return bool( Parsers::raw );
    }

    /**
     * Creates a parser that receives one of two values depending on the argument.
     *
     * @param <T> The parsed argument type.
     * @param trueValue The value to receive if the argument is {@code true}.
     * @param falseValue The value to receive if the argument is {@code false}.
     * @return The parser.
     */
    @SideEffectFree
    public static <T extends @NonNull Object> BooleanParser<T> bool( 
            final T trueValue, final T falseValue ) {

        return bool( simple( v -> v ? trueValue : falseValue ) );

    }

    /**
     * Creates a parser that uses the given function to parse received values.
     *
     * @param <T> The parsed argument type.
     * @param parser The parser to use.
     * @return The parser.
     */
    @SideEffectFree
    public static <T extends @NonNull Object> BooleanParser<T> bool( 
            final ParserFunction<Boolean, T> parser ) {

        return new BooleanParserImpl<>( parser );

    }

    /* Integers */

    /**
     * Creates a parser that receives plain integer values.
     *
     * @return The parser.
     */
    @SideEffectFree
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
    @SideEffectFree
    public static <T extends @NonNull Object> IntegerParser<T> integer( 
            final ParserFunction<Long, T> parser ) {

        return new IntegerParserImpl<>( null, null, null, parser );

    }

    /**
     * Creates a parser that receives plain integer values from within a set of choices.
     *
     * @param choices The allowed values.
     * @return The parser.
     * @throws IllegalArgumentException if the choice list is empty.
     */
    @SideEffectFree
    public static IntegerParser<Long> integer( final @MinLen( 1 ) List<Choice<Long>> choices ) 
            throws IllegalArgumentException {

        return new IntegerParserImpl<>( 
                choices, 
                null, null, 
                Parsers::raw
        );

    }

    /**
     * Creates a parser that receives plain integer values from within a set of choices.
     *
     * @param choices The allowed values.
     * @return The parser.
     * @throws IllegalArgumentException if the choice list is empty.
     */
    @SideEffectFree
    @SafeVarargs
    @SuppressWarnings( { "varargs", "argument" } ) // Idk why the @MinLen doesn't propagate
    public static IntegerParser<Long> integer( final Choice<Long> @MinLen( 1 )... choices ) 
            throws IllegalArgumentException {

        return integer( Arrays.asList( choices ) );

    }

    /**
     * Creates a parser that receives a value from within a set of choices.
     *
     * @param <T> The parsed argument type.
     * @param choices The choices.
     * @return The parser.
     * @throws IllegalArgumentException if the choice list is empty.
     */
    @SideEffectFree
    public static <T extends @NonNull Object> IntegerParser<T> choiceInteger( 
            final @MinLen( 1 ) List<Entry<Choice<Long>, T>> choices ) 
            throws IllegalArgumentException {

        return new IntegerParserImpl<>( 
                choiceEntries( choices ), 
                null, null, 
                choiceParser( choices ) 
        );

    }

    /**
     * Creates a parser that receives a value from within a set of choices.
     *
     * @param <T> The parsed argument type.
     * @param choices The choices.
     * @return The parser.
     * @throws IllegalArgumentException if the choice list is empty.
     */
    @SideEffectFree
    @SafeVarargs
    @SuppressWarnings( { "varargs", "argument" } ) // Idk why the @MinLen doesn't propagate
    public static <T extends @NonNull Object> IntegerParser<T> choiceInteger( 
            final Entry<Choice<Long>, T> @MinLen( 1 )... choices ) 
            throws IllegalArgumentException {

        return choiceInteger( Arrays.asList( choices ) );

    }

    /**
     * Creates a parser that receives plain integer values, which must be at least
     * the given value.
     *
     * @param minimum The minimum value allowed (inclusive).
     * @return The parser.
     */
    @SideEffectFree
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
    @SideEffectFree
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
    @SideEffectFree
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
    @SideEffectFree
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
    @SideEffectFree
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
    @SideEffectFree
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
    @SideEffectFree
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
    @SideEffectFree
    public static <T extends @NonNull Object> FloatParser<T> number( 
            final ParserFunction<Double, T> parser ) {

        return new FloatParserImpl<>( null, null, null, parser );
        
    }

    /**
     * Creates a parser that receives plain floating-point values from within a set of choices.
     *
     * @param choices The allowed values.
     * @return The parser.
     * @throws IllegalArgumentException if the choice list is empty.
     */
    @SideEffectFree
    public static FloatParser<Double> number( final @MinLen( 1 ) List<Choice<Double>> choices ) 
            throws IllegalArgumentException {

        return new FloatParserImpl<>( 
                choices, 
                null, null, 
                Parsers::raw
        );

    }

    /**
     * Creates a parser that receives plain floating-point values from within a set of choices.
     *
     * @param choices The allowed values.
     * @return The parser.
     * @throws IllegalArgumentException if the choice list is empty.
     */
    @SideEffectFree
    @SafeVarargs
    @SuppressWarnings( { "varargs", "argument" } ) // Idk why the @MinLen doesn't propagate
    public static FloatParser<Double> number( final Choice<Double> @MinLen( 1 )... choices ) 
            throws IllegalArgumentException {

        return number( Arrays.asList( choices ) );

    }

    /**
     * Creates a parser that receives a value from within a set of choices.
     *
     * @param <T> The parsed argument type.
     * @param choices The choices.
     * @return The parser.
     * @throws IllegalArgumentException if the choice list is empty.
     */
    @SideEffectFree
    public static <T extends @NonNull Object> FloatParser<T> numberChoice( 
            final @MinLen( 1 ) List<Entry<Choice<Double>, T>> choices )
            throws IllegalArgumentException {

        return new FloatParserImpl<>( 
                choiceEntries( choices ), 
                null, null, 
                choiceParser( choices ) 
        );

    }

    /**
     * Creates a parser that receives a value from within a set of choices.
     *
     * @param <T> The parsed argument type.
     * @param choices The choices.
     * @return The parser.
     * @throws IllegalArgumentException if the choice list is empty.
     */
    @SideEffectFree
    @SafeVarargs
    @SuppressWarnings( { "varargs", "argument" } ) // Idk why the @MinLen doesn't propagate
    public static <T extends @NonNull Object> FloatParser<T> numberChoice( 
            final Entry<Choice<Double>, T> @MinLen( 1 )... choices )
            throws IllegalArgumentException {

        return numberChoice( Arrays.asList( choices ) );

    }

    /**
     * Creates a parser that receives plain floating-point values, which must be at least
     * the given value.
     *
     * @param minimum The minimum value allowed (inclusive).
     * @return The parser.
     */
    @SideEffectFree
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
    @SideEffectFree
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
    @SideEffectFree
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
    @SideEffectFree
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
    @SideEffectFree
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
    @SideEffectFree
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
    @SideEffectFree
    public static StringParser<String> string() {
        return string( Parsers::raw );
    }

    /**
     * Creates a parser that receives plain string values.
     *
     * @param minLength The minimum allowed length, or {@code null} if none.
     * @param maxLength The maximum allowed length, or {@code null} if none.
     * @return The parser.
     */
    @SideEffectFree
    public static StringParser<String> string(
            final @Nullable @IntRange( from = 0, to = StringParser.MAX_LENGTH ) Integer minLength,
            final @Nullable @IntRange( from = 1, to = StringParser.MAX_LENGTH ) Integer maxLength
    ) {
        return string( Parsers::raw, minLength, maxLength );
    }

    /**
     * Creates a parser that uses the given function to parse received values.
     *
     * @param <T> The parsed argument type.
     * @param parser The parser to use.
     * @return The parser.
     */
    @SideEffectFree
    public static <T extends @NonNull Object> StringParser<T> string( 
            final ParserFunction<String, T> parser ) {
        return string( parser, null, null );
    }

    /**
     * Creates a parser that uses the given function to parse received values.
     *
     * @param <T> The parsed argument type.
     * @param parser The parser to use.
     * @param minLength The minimum allowed length, or {@code null} if none.
     * @param maxLength The maximum allowed length, or {@code null} if none.
     * @return The parser.
     */
    @SideEffectFree
    public static <T extends @NonNull Object> StringParser<T> string( 
            final ParserFunction<String, T> parser,
            final @Nullable @IntRange( from = 0, to = StringParser.MAX_LENGTH ) Integer minLength,
            final @Nullable @IntRange( from = 1, to = StringParser.MAX_LENGTH ) Integer maxLength
    ) {

        return new StringParserImpl<>( null, parser, minLength, maxLength, false );
        
    }

    /**
     * Creates a parser that receives plain string values from within a set of choices.
     *
     * @param choices The allowed values.
     * @return The parser.
     * @throws IllegalArgumentException if the choice list is empty.
     */
    @SideEffectFree
    public static StringParser<String> string( final @MinLen( 1 ) List<Choice<String>> choices )
            throws IllegalArgumentException {

        return new StringParserImpl<>( 
                choices,
                Parsers::raw,
                null, null, false
        );

    }

    /**
     * Creates a parser that receives plain string values from within a set of choices.
     *
     * @param choices The allowed values.
     * @return The parser.
     * @throws IllegalArgumentException if the choice list is empty.
     */
    @SideEffectFree
    @SafeVarargs
    @SuppressWarnings( { "varargs", "argument" } ) // Idk why the @MinLen doesn't propagate
    public static StringParser<String> string( final Choice<String> @MinLen( 1 )... choices )
            throws IllegalArgumentException {

        return string( Arrays.asList( choices ) );

    }

    /**
     * Creates a parser that receives a value from within a set of choices.
     *
     * @param <T> The parsed argument type.
     * @param choices The choices.
     * @return The parser.
     * @throws IllegalArgumentException if the choice list is empty.
     */
    @SideEffectFree
    public static <T extends @NonNull Object> StringParser<T> stringChoice( 
            final @MinLen( 1 ) List<Entry<Choice<String>, T>> choices )
            throws IllegalArgumentException {

        return new StringParserImpl<>( 
                choiceEntries( choices ), 
                choiceParser( choices ),
                null, null, false
        );

    }

    /**
     * Creates a parser that receives a value from within a set of choices.
     *
     * @param <T> The parsed argument type.
     * @param choices The choices.
     * @return The parser.
     * @throws IllegalArgumentException if the choice list is empty.
     */
    @SideEffectFree
    @SafeVarargs
    @SuppressWarnings( { "varargs", "argument" } ) // Idk why the @MinLen doesn't propagate
    public static <T extends @NonNull Object> StringParser<T> stringChoice( 
            final Entry<Choice<String>, T> @MinLen( 1 )... choices )
            throws IllegalArgumentException {

        return stringChoice( Arrays.asList( choices ) );

    }

    /* Long Strings */

    /**
     * Creates a parser that receives plain string values.
     * 
     * <p>Unlike the {@link #string()} variant, this parser allows 
     * {@link StringParser#allowMerge() merging}.
     *
     * @return The parser.
     * @see StringParser#allowMerge()
     */
    @SideEffectFree
    public static StringParser<String> text() {
        return text( Parsers::raw );
    }

    /**
     * Creates a parser that receives plain string values.
     * 
     * <p>Unlike the {@link #string(Integer, Integer)} variant, this parser allows 
     * {@link StringParser#allowMerge() merging}.
     *
     * @param minLength The minimum allowed length, or {@code null} if none.
     * @param maxLength The maximum allowed length, or {@code null} if none.
     * @return The parser.
     * @see StringParser#allowMerge()
     */
    @SideEffectFree
    public static StringParser<String> text(
            final @Nullable @IntRange( from = 0, to = StringParser.MAX_LENGTH ) Integer minLength,
            final @Nullable @IntRange( from = 1, to = StringParser.MAX_LENGTH ) Integer maxLength
    ) {
        return text( Parsers::raw, minLength, maxLength );
    }

    /**
     * Creates a parser that uses the given function to parse received values.
     * 
     * <p>Unlike the {@link #string(ParserFunction)} variant, this parser allows 
     * {@link StringParser#allowMerge() merging}.
     *
     * @param <T> The parsed argument type.
     * @param parser The parser to use.
     * @return The parser.
     * @see StringParser#allowMerge()
     */
    @SideEffectFree
    public static <T extends @NonNull Object> StringParser<T> text( 
            final ParserFunction<String, T> parser ) {
        return text( parser, null, null );
    }

    /**
     * Creates a parser that uses the given function to parse received values.
     * 
     * <p>Unlike the {@link #string(ParserFunction, Integer, Integer)} variant, this parser allows
     * {@link StringParser#allowMerge() merging}.
     *
     * @param <T> The parsed argument type.
     * @param parser The parser to use.
     * @param minLength The minimum allowed length, or {@code null} if none.
     * @param maxLength The maximum allowed length, or {@code null} if none.
     * @return The parser.
     * @see StringParser#allowMerge()
     */
    @SideEffectFree
    public static <T extends @NonNull Object> StringParser<T> text( 
            final ParserFunction<String, T> parser,
            final @Nullable @IntRange( from = 0, to = StringParser.MAX_LENGTH ) Integer minLength,
            final @Nullable @IntRange( from = 1, to = StringParser.MAX_LENGTH ) Integer maxLength
    ) {

        return new StringParserImpl<>( null, parser, minLength, maxLength, true );
        
    }

    /* Attachments */

    /**
     * Creates a parser that receives raw attachment values.
     *
     * @return The parser.
     */
    @SideEffectFree
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
    @SideEffectFree
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
    @SideEffectFree
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
    @SideEffectFree
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
    @SideEffectFree
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
    @SideEffectFree
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
    @SideEffectFree
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
    @SideEffectFree
    public static TextFileParser<String> textFile( final @IntRange( from = 0 ) int maxSize ) {

        return textFile( maxSize, Parsers::raw );

    }

    /**
     * Creates a parser that receives the plain contents of a text file, with unbounded file size.
     *
     * @return The parser.
     */
    @SideEffectFree
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
    @SideEffectFree
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
    @SideEffectFree
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
    @SideEffectFree
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
    @SideEffectFree
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
    @SideEffectFree
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
    @SideEffectFree
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
    @SideEffectFree
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
    @SideEffectFree
    public static SnowflakeParser<Snowflake> channelId() {

        return channelId( Parsers::raw );

    }

    /* Entities */

    /**
     * Creates a parser that receives raw user values.
     *
     * @return The parser.
     */
    @SideEffectFree
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
    @SideEffectFree
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
    @SideEffectFree
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
    @SideEffectFree
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
    @SideEffectFree
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
    @SideEffectFree
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
    @SideEffectFree
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
    @SideEffectFree
    public static <C extends @NonNull Channel, T extends @NonNull Object> 
            ChannelArgumentParser<C, T> channel( final Class<C> type,
            final ParserFunction<C, T> parser
    ) {

        return new ChannelParserImpl<>( type, parser );

    }

    /* Lists */

    /**
     * Creates a list parser with string items.
     *
     * @return The parser.
     */
    @SideEffectFree
    public static ListParser<String> list() {

        return list( Parsers::raw );

    }

    /**
     * Creates a list parser with string items.
     *
     * @param minItems The minimum number of items allowed. Note that, when setting to 0,
     *                 it is recommended to set the associated parameter to be non-required
     *                 with an empty list as default value. See {@link ListParser#minItems()}
     *                 for details.
     * @param maxItems The maximum number of items allowed.
     * @return The parser.
     */
    @SideEffectFree
    public static ListParser<String> list(
            final @IntRange( from = 0, to = Integer.MAX_VALUE ) int minItems,
            final @IntRange( from = 1, to = Integer.MAX_VALUE ) int maxItems
    ) {

        return list( Parsers::raw, minItems, maxItems );

    }

    /**
     * Creates a list parser that uses the given function to parse items.
     *
     * @param <T> The item type.
     * @param parser The parser to use.
     * @return The parser.
     */
    @SideEffectFree
    public static <T extends @NonNull Object> ListParser<T> list(
            final ParserFunction<String, T> parser
    ) {

        return list( parser, 1, Integer.MAX_VALUE );

    }

    /**
     * Creates a list parser that uses the given function to parse items.
     *
     * @param <T> The item type.
     * @param parser The parser to use.
     * @param minItems The minimum number of items allowed. Note that, when setting to 0,
     *                 it is recommended to set the associated parameter to be non-required
     *                 with an empty list as default value. See {@link ListParser#minItems()}
     *                 for details.
     * @param maxItems The maximum number of items allowed.
     * @return The parser.
     */
    @SideEffectFree
    public static <T extends @NonNull Object> ListParser<T> list(
            final ParserFunction<String, T> parser,
            final @IntRange( from = 0, to = Integer.MAX_VALUE ) int minItems,
            final @IntRange( from = 1, to = Integer.MAX_VALUE ) int maxItems
    ) {

        return new ListParserImpl<>( parser, minItems, maxItems );

    }

    /* Parser adapters */

    /**
     * A parser that does not depend on the invocation context.
     *
     * @param <T> The type of argument that is provided.
     * @param <R> The type of raw argument that is received.
     * @version 1.0
     * @since 1.0
     */
    public interface Functor<R extends @NonNull Object, T extends @NonNull Object> 
            extends ParserFunction<R, T> {

        /**
         * Parses the given raw argument from the user into the corresponding value.
         *
         * @param raw The raw argument received from the user.
         * @return A Mono that issues the parsed argument. If the raw value is invalid, it may
         *         fail with a {@link InvalidArgumentException}.
         * @throws InvalidArgumentException if the given argument is not a valid value.
         */
        @SideEffectFree
        Mono<T> parse( R raw ) throws InvalidArgumentException;

        @Override
        default Mono<T> parse( final CommandContext context, final R raw ) 
                throws InvalidArgumentException {
            return parse( raw );
        }

    }

    /**
     * A parser that executes synchronously.
     *
     * @param <T> The type of argument that is provided.
     * @param <R> The type of raw argument that is received.
     * @version 1.0
     * @since 1.0
     */
    public interface Synchronous<R extends @NonNull Object, T extends @NonNull Object> 
            extends ParserFunction<R, T> {

        /**
         * Parses the given raw argument from the user into the corresponding value.
         *
         * @param context The execution context.
         * @param raw The raw argument received from the user.
         * @return The parsed argument. If the raw value is invalid, it may
         *         fail with a {@link InvalidArgumentException}.
         * @throws InvalidArgumentException if the given argument is not a valid value.
         */
        @SideEffectFree
        T parseNow( CommandContext context, R raw ) throws InvalidArgumentException;

        @Override
        default Mono<T> parse( final CommandContext context, final R raw ) 
                throws InvalidArgumentException {
            return Mono.just( parseNow( context, raw ) );
        }

    }

    /**
     * A parser that executes synchronously and does not depend on the invocation context.
     *
     * @param <T> The type of argument that is provided.
     * @param <R> The type of raw argument that is received.
     * @version 1.0
     * @since 1.0
     */
    public interface Simple<R extends @NonNull Object, T extends @NonNull Object> 
            extends ParserFunction<R, T> {

        /**
         * Parses the given raw argument from the user into the corresponding value.
         *
         * @param raw The raw argument received from the user.
         * @return The parsed argument. If the raw value is invalid, it may
         *         fail with a {@link InvalidArgumentException}.
         * @throws InvalidArgumentException if the given argument is not a valid value.
         */
        @SideEffectFree
        T parseNow( R raw ) throws InvalidArgumentException;

        @Override
        default Mono<T> parse( final CommandContext context, final R raw ) 
                throws InvalidArgumentException {
            return Mono.just( parseNow( raw ) );
        }

    }

    /* Implementation classes */

    /**
     * A parser for boolean values.
     *
     * @param <T> The argument type.
     * @param parser The function to use to parse values.
     * @since 1.0
     */    
    private record BooleanParserImpl<T extends @NonNull Object>(
            ParserFunction<Boolean, T> parser
    ) implements BooleanParser<T> {

        @Override
        public Mono<T> parseArgument( final CommandContext context, final Boolean raw ) {

            return parser.parse( context, raw );

        }

    }

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
        public Mono<T> parseArgument( final CommandContext context, final Long raw ) 
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
        public Mono<T> parseArgument( final CommandContext context, final Double raw ) 
                throws InvalidArgumentException {

            return parser.parse( context, raw );

        }

    }

    /**
     * A parser for string values.
     *
     * @param <T> The argument type.
     * @param choices The allowed values.
     * @param parser The function to use to parse values.
     * @param minLength The minimum length.
     * @param maxLength The maximum length.
     * @param allowMerge Whether to allow merging behavior.
     * @since 1.0
     */
    private record StringParserImpl<T extends @NonNull Object>(
            @Nullable List<Choice<String>> choices,
            ParserFunction<String, T> parser,
            @IntRange( from = 0, to = StringParser.MAX_LENGTH ) @Nullable Integer minLength,
            @IntRange( from = 1, to = StringParser.MAX_LENGTH ) @Nullable Integer maxLength,
            boolean allowMerge
    ) implements StringParser<T> {

        @Override
        public Mono<T> parseArgument( final CommandContext context, final String raw ) 
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
        public Mono<T> parseArgument( final CommandContext context, final Attachment raw ) 
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
        public Mono<T> parseArgument( final CommandContext context, final Snowflake raw )
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
        public Mono<T> parseArgument( final CommandContext context, final C raw )
                throws InvalidArgumentException {

            return parser.parse( context, raw );

        }

    }

    /**
     * A parser for list values.
     *
     * @param <T> The argument type.
     * @param parser The function to use to parse values.
     * @param minItems The minimum number of items.
     * @param maxItems The maximum number of items.
     * @since 1.0
     */
    private record ListParserImpl<T extends @NonNull Object>(
            ParserFunction<String, T> parser,
            @IntRange( from = 0, to = Integer.MAX_VALUE ) int minItems,
            @IntRange( from = 1, to = Integer.MAX_VALUE ) int maxItems
    ) implements ListParser<T> {

        @Override
        public Mono<T> parseItem( final CommandContext context, final String raw ) {

            return parser.parse( context, raw );

        }

    }
    
}
