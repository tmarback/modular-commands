package dev.sympho.modular_commands.utils.parse;

import java.util.List;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.context.CommandContext;
import dev.sympho.modular_commands.api.command.parameter.parse.InvalidArgumentException;
import dev.sympho.modular_commands.api.command.parameter.parse.ParserFunction;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * A parser wrapper that tolerates errors by returning a result that reports either
 * the parsed item or an encountered error.
 * 
 * <p>This allows for the receiver of the argument to do their own handling of
 * encountered errors rather than immediately stopping processing.
 *
 * @param <R> The raw type.
 * @param <T> The parsed type.
 * @version 1.0
 * @since 1.0
 */
@FunctionalInterface
public interface TryParser<R extends @NonNull Object, T extends @NonNull Object> 
        extends ParserFunction<R, TryParser.Result<R, T>> {

    /**
     * Creates a try-parser wrapper the given parser.
     *
     * @param <R> The raw type.
     * @param <T> The parsed type.
     * @param parser The parser to wrap.
     * @return The try-parser.
     */
    @SideEffectFree
    static <R extends @NonNull Object, T extends @NonNull Object> TryParser<R, T> of(
            ParserFunction<R, T> parser
    ) {

        return () -> parser;

    }

    /**
     * Extracts the items in a result list that were successfully parsed.
     * 
     * <p>The returned list contains the items in the same relative order that they
     * appear in the given list.
     *
     * @param <R> The raw type.
     * @param <T> The parsed type.
     * @param results The list of results.
     * @return The successfully parsed items.
     */
    @SideEffectFree
    static <R extends @NonNull Object, T extends @NonNull Object> List<T> items(
            final List<Result<R, T>> results ) {

        return results.stream()
                .filter( i -> i instanceof Success<R, T> )
                .map( i -> ( Success<R, T> ) i )
                .map( i -> i.item() )
                .toList();

    }

    /**
     * Extracts the items in a result list that had errors.
     * 
     * <p>The returned list contains the items in the same relative order that they
     * appear in the given list.
     *
     * @param <R> The raw type.
     * @param <T> The parsed type.
     * @param results The list of results.
     * @return The invalid items.
     */
    @SideEffectFree
    static <R extends @NonNull Object, T extends @NonNull Object> List<Failure<R, T>> 
            errors( final List<Result<R, T>> results ) {

        return results.stream()
                .filter( i -> i instanceof Failure<R, T> )
                .map( i -> ( Failure<R, T> ) i )
                .toList();

    }

    /**
     * Splits the given result list into a list of successfully parsed items and a
     * list of failed items.
     * 
     * <p>The returned lists contains the items in the same relative order that they
     * appear in the given list.
     *
     * @param <R> The raw type.
     * @param <T> The parsed type.
     * @param results The list of results.
     * @return The parsed and invalid items as discrete lists.
     */
    @SideEffectFree
    static <R extends @NonNull Object, T extends @NonNull Object> 
            Tuple2<List<T>, List<Failure<R, T>>> split( final List<Result<R, T>> results ) {

        return Tuples.of( items( results ), errors( results ) );
    
    }

    /**
     * The parser to delegate to.
     *
     * @return The parser.
     */
    @Pure
    ParserFunction<R, T> parser();

    @Override
    default Mono<Result<R, T>> parse( final CommandContext context, final R item ) 
            throws InvalidArgumentException {

        final var parser = parser();
        return Mono.defer( () -> parser.apply( context, item ) )
                .map( i -> ( Result<R, T> ) new Success<R, T>( item, i ) )
                .onErrorResume( InvalidArgumentException.class, 
                        error -> Mono.just( new Failure<R, T>( item, error ) ) );

    }

    /**
     * The result of parsing.
     * 
     * <p>Note that, for a given instance, exactly one of {@link #item()} or {@link #error()}
     * will return {@code null}.
     *
     * @param <R> The raw type.
     * @param <T> The parsed type.
     * @since 1.0
     */
    sealed interface Result<R extends @NonNull Object, T extends @NonNull Object> 
            permits Success, Failure {

        /**
         * The raw value used in parsing.
         *
         * @return The raw value.
         */
        @Pure
        R raw();

        /**
         * The parsed item, if parsed successfully.
         *
         * @return The parsed item, or {@code null} if the item was invalid.
         */
        @Pure
        @Nullable T item();

        /**
         * The error encountered in parsing, if any.
         *
         * @return The error, or {@code null} if the item was parsed successfully.
         */
        @Pure
        @Nullable InvalidArgumentException error();

    }

    /**
     * A result in which the item was parsed successfully.
     *
     * @param <R> The raw type.
     * @param <T> The parsed type.
     * @param raw The raw value.
     * @param item The parsed item.
     * @since 1.0
     */
    record Success<R extends @NonNull Object, T extends @NonNull Object>(
            R raw,
            T item
    ) implements Result<R, T> {

        /**
         * @implSpec Always returns {@code null}.
         */
        @Override
        public @Nullable InvalidArgumentException error() {
            return null;
        }

    }

    /**
     * A result in which the item was invalid.
     *
     * @param <R> The raw type.
     * @param <T> The parsed type.
     * @param raw The raw value.
     * @param error The parsing error.
     * @since 1.0
     */
    record Failure<R extends @NonNull Object, T extends @NonNull Object>(
            R raw,
            InvalidArgumentException error
    ) implements Result<R, T> {

        /**
         * @implSpec Always returns {@code null}.
         */
        @Override
        public @Nullable T item() {
            return null;
        }

    }
    
}
