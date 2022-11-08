package dev.sympho.modular_commands.utils;

import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import reactor.core.publisher.Flux;

/**
 * Provides implementations for iterators that are always empty.
 *
 * @version 1.0
 * @since 1.0
 * @apiNote These classes are an optimization on the case where an empty iterator is
 *          required.
 */
final class EmptyIterators {

    /** Do not instantiate. */
    private EmptyIterators() {}

    /**
     * Detachable smart iterator.
     *
     * @param <E> The element type.
     * @since 1.0
     */
    public static class EmptyDetachable<E extends @NonNull Object> 
            implements SmartIterator.Detachable<E> {

        /** The global instance. */
        @SuppressWarnings( "rawtypes" )
        public static final EmptyDetachable INSTANCE = new EmptyDetachable();

        /** Only use global instance. */
        protected EmptyDetachable() {}

        @Override
        public E next() throws NoSuchElementException {
            throw new NoSuchElementException( NO_MORE_ELEMENTS_ERROR );
        }

        @Override
        public @Nullable E peek() {
            return null;
        }

        @Override
        public Detachable<E> toIterator() {
            return this;
        }

        @Override
        public Spliterator<E> toSpliterator() {
            return Spliterators.emptySpliterator();
        }

        @Override
        public Stream<E> toStream() {
            return Stream.empty();
        }

        @Override
        public Flux<E> toFlux() {
            return Flux.empty();
        }

    }

    /**
     * Async string splitter iterator.
     *
     * @since 1.0
     */
    public static class EmptySplitter extends EmptyDetachable<String> 
            implements StringSplitter.Async.Iterator {

        /** The global instance. */
        public static final EmptySplitter INSTANCE = new EmptySplitter();

        /** Only use global instance. */
        protected EmptySplitter() {}

        @Override
        public String remainder() {
            return "";
        }
    
    }
    
}
