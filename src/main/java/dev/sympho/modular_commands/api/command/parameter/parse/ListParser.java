package dev.sympho.modular_commands.api.command.parameter.parse;

import java.util.List;
import java.util.function.Function;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.common.value.qual.IntRange;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.context.CommandContext;
import dev.sympho.modular_commands.utils.StringSplitter;
import dev.sympho.modular_commands.utils.parse.TryParser;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * A parser that extracts lists of objects by splitting a string argument and parsing each item.
 * 
 * <p>Items appear in the resulting list in the same order that they appear in the raw argument.
 *
 * @param <T> The element type.
 * @version 1.0
 * @since 1.0
 */
@FunctionalInterface
public interface ListParser<T extends @NonNull Object> extends StringParser<List<T>> {

    /** The default splitter ({@link StringSplitter.Shell}). */
    StringSplitter.Async DEFAULT_SPLITTER = new StringSplitter.Shell();

    /**
     * Applies pre-processing steps to the received argument before splitting, such as cleaning
     * the string.
     *
     * @param arg The received argument.
     * @return The pre-processed argument.
     * @implNote The default is to trim the string, removing leading and trailing whitespace.
     */
    @SideEffectFree
    default String preprocess( final String arg ) {
        return arg.trim();
    }

    /**
     * Gives the splitter to use for splitting received strings.
     *
     * @return The splitter to use.
     * @implSpec The default is {@link #DEFAULT_SPLITTER}.
     */
    @Pure
    default StringSplitter.Async splitter() {
        return DEFAULT_SPLITTER;
    }

    /**
     * Determines whether the parser should fail on the first element that encounters an
     * error ({@code true}), or finish parsing all elements before reporting an
     * {@link InvalidListException} with all encountered errors ({@code false}).
     *
     * @return Whether to fail as soon as any error is encountered.
     * @implSpec The default is {@code true}.
     */
    default boolean failFast() {
        return true;
    }

    /**
     * The minimum amount of items allowed (inclusive).
     * 
     * <p>Note that, while this value may technically be 0, the only way for an
     * empty list to be received is through a message-based invocation, where 
     * {@link #allowMerge() merging} does not occur, and the user uses quotations
     * to provide an empty string, as in all other scenarios the argument would
     * simply be missing. Due to that, it is strongly recommended to have an
     * empty list as a default value (and not set the parameter as required)
     * when setting the minimum to 0, in order to maintain consistency.
     *
     * @return The amount.
     * @implSpec The default is 1. Must be between 0 and {@value Integer#MAX_VALUE}.
     */
    @Pure
    @IntRange( from = 0, to = Integer.MAX_VALUE )
    default int minItems() {
        return 1;
    }

    /**
     * The maximum amount of items allowed (inclusive).
     *
     * @return The amount.
     * @implSpec The default is {@value Integer.MAX_VALUE}. 
     *           Must be between 1 and {@value Integer#MAX_VALUE}.
     */
    @Pure
    @IntRange( from = 1, to = Integer.MAX_VALUE )
    default int maxItems() {
        return Integer.MAX_VALUE;
    }

    /**
     * @implSpec Defaults to {@code true}.
     * @implNote Since lists are split by spaces by default, without trailing arg merging
     *           they would need to always be specified with quotes, which is doubly
     *           problematic since items within it may also need quotes. Thus, allowing
     *           merge behavior is recommended.
     */
    @Override
    default boolean allowMerge() {
        return true;
    }

    /**
     * Parses an individual item in the list.
     *
     * @param context The invocation context.
     * @param raw The raw item.
     * @return The parsed item.
     */
    @SideEffectFree
    Mono<T> parseItem( CommandContext context, String raw );

    /**
     * Parses all the found elements.
     *
     * @param context The invocation context.
     * @param raws The raw items.
     * @return The parsed items.
     */
    private Flux<T> parseAll( final CommandContext context, final Flux<String> raws ) {

        if ( failFast() ) {
            return raws.concatMap( i -> parseItem( context, i ) );
        } else {
            final var parser = TryParser.of( this::parseItem );
            return raws.concatMap( i -> parser.apply( context, i ) )
                    .collectList()
                    .map( TryParser::split )
                    .flatMap( r -> r.getT2().isEmpty() 
                            ? Mono.just( r.getT1() ) 
                            : Mono.error( new InvalidListException( r.getT2() ) )
                    )
                    .flatMapIterable( Function.identity() );
        }

    }

    @Override
    default Mono<List<T>> parseArgument( final CommandContext context, final String raw ) 
            throws InvalidArgumentException {

        final var target = preprocess( raw );

        if ( target.isEmpty() ) {
            return Mono.just( List.of() );
        }

        return splitter().splitAsync( target )
                .index()
                .flatMap( v -> v.getT1() < maxItems() 
                        ? Mono.just( v.getT2() ) 
                        : Mono.error( new InvalidArgumentException( String.format( 
                                "Too many items, must be at most %d", maxItems()
                        ) ) ) 
                )
                .transform( f -> parseAll( context, f ) )
                .collectList()
                .flatMap( args -> args.size() >= minItems()
                        ? Mono.just( args )
                        : Mono.error( new InvalidArgumentException( String.format(
                                "Not enough items, must be at least %d", minItems()
                        ) ) ) 
                );

    }
    
}
