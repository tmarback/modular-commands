package dev.sympho.modular_commands.utils;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.checkerframework.checker.nullness.qual.EnsuresNonNullIf;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

import reactor.core.publisher.Flux;

/**
 * An iterator with extra capabilities. Note that it does not support
 * {@code null} elements, and is not thread-safe by default.
 *
 * @param <E> The element type.
 * @version 1.0
 * @since 1.0
 */
public interface SmartIterator<E extends @NonNull Object> extends Iterator<E> {

    /** Exception message for when there are no more elements. */
    String NO_MORE_ELEMENTS_ERROR = "No more elements";

    /**
     * Returns an empty iterator.
     *
     * @param <E> The element type.
     * @return An empty iterator.
     */
    @SuppressWarnings( "unchecked" )
    static <E extends @NonNull Object> SmartIterator.Detachable<E> empty() {
        return ( SmartIterator.Detachable<E> ) EmptyIterators.EmptyDetachable.INSTANCE;
    }

    /**
     * Retrives the element that will be returned by the next call to {@link #next()},
     * without advancing the iterator.
     *
     * @return The next element, or {@code null} if there are no elements remaining.
     */
    @Pure
    @Nullable E peek();

    /**
     * Returns a spliterator that iterates over the remaining elements to be
     * traversed by this iterator.
     * 
     * <p>Note that this interface makes no promises as to whether it is safe to
     * continue using this iterator while using the returned spliterator; it is
     * left as an implementation detail.
     *
     * @return The spliterator.
     * @implSpec The default implementation creates a spliterator backed by this
     *           iterator; as such, their state is interdependent, and continuing
     *           to use this iterator results in undefined behavior.
     */
    @SideEffectFree
    default Spliterator<E> toSpliterator() {

        return Spliterators.spliteratorUnknownSize( 
                this, 
                Spliterator.NONNULL
        );

    }

    /**
     * Returns a stream that contains the remaining elements to be
     * traversed by this iterator.
     * 
     * <p>Note that this interface makes no promises as to whether it is safe to
     * continue using this iterator while using the returned stream; it is
     * left as an implementation detail.
     *
     * @return The stream.
     * @implSpec The default implementation creates a stream backed by this
     *           iterator; as such, their state is interdependent, and continuing
     *           to use this iterator results in undefined behavior.
     */
    @SideEffectFree
    default Stream<E> toStream() {

        return StreamSupport.stream( toSpliterator(), false );

    }

    /**
     * Returns a flux that issues the remaining elements to be
     * traversed by this iterator.
     * 
     * <p>Note that this interface makes no promises as to whether it is safe to
     * continue using this iterator while using the returned flux; it is
     * left as an implementation detail.
     *
     * @return The flux.
     * @implSpec The default implementation creates a stream backed by this
     *           iterator; as such, their state is interdependent, and continuing
     *           to use this iterator results in undefined behavior.
     */
    @SideEffectFree
    default Flux<E> toFlux() {

        return Flux.fromStream( toStream() );

    }

    @Override
    @EnsuresNonNullIf( expression = "peek()", result = true )
    default boolean hasNext() {

        return peek() != null;

    }

    /**
     * Creates a smart iterator backed by the given iterator.
     *
     * @param <E> The element type.
     * @param backing The backing iterator.
     * @return The smart iterator.
     */
    @SideEffectFree
    static <E extends @NonNull Object> SmartIterator<E> from( final Iterator<E> backing ) {

        return new Wrapper<>( backing );

    }

    /**
     * Creates a smart iterator that iterates over the contents of the given list.
     *
     * @param <E> The element type.
     * @param list The list to iterate over.
     * @return The smart iterator.
     */
    @SideEffectFree
    static <E extends @NonNull Object> Detachable<E> from( final List<E> list ) {

        return new ListIterator<>( list );

    }

    /**
     * A smart iterator that, when creating other iterators/streams/fluxes/etc from its current
     * state, makes them <i>detached</i>, so that they are independent of the future state of
     * the original iterator and vice-versa.
     *
     * @param <E> The element type.
     * @since 1.0
     */
    interface Detachable<E extends @NonNull Object> extends SmartIterator<E> {

        /**
         * Returns an iterator that iterates over the remaining elements to be
         * traversed by this iterator.
         * 
         * <p>The returned iterator is guaranteed to be <i>detached</i> from this
         * iterator. In other words, they are completely independent of each other
         * and may both be used separately.
         *
         * @return The iterator.
         */
        @SideEffectFree
        Detachable<E> toIterator();

        /**
         * Returns a spliterator that iterates over the remaining elements to be
         * traversed by this iterator.
         * 
         * <p>The returned spliterator is guaranteed to be <i>detached</i> from this
         * iterator. In other words, they are completely independent of each other
         * and may both be used separately.
         */
        @Override
        Spliterator<E> toSpliterator();

        /**
         * Returns a stream that contains over the remaining elements to be
         * traversed by this iterator.
         * 
         * <p>The returned stream is guaranteed to be <i>detached</i> from this
         * iterator. In other words, they are completely independent of each other
         * and may both be used separately.
         */
        @Override
        Stream<E> toStream();

        /**
         * Returns a flux that issues the remaining elements to be
         * traversed by this iterator.
         * 
         * <p>The returned flux is guaranteed to be <i>detached</i> from this
         * iterator. In other words, they are completely independent of each other
         * and may both be used separately.
         */
        @Override
        Flux<E> toFlux();

    }

    /**
     * A smart iterator that wraps an existing iterator.
     * 
     * <p>Note that, in order to support {@link #peek()} functionality, the wrapper always
     * prefetches the next element. This may cause issues with iterators whose {@link #next()}
     * method has side effects that are timing-sensitive.
     * 
     * <p>Note also that, naturally, it only tolerates concurrent modifications to the original
     * data source during iteration if the backing iterator does so.
     *
     * @param <E> The element type.
     * @since 1.0
     */
    class Wrapper<E extends @NonNull Object> implements SmartIterator<E> {

        /** The backing iterator. */
        private final Iterator<E> backing;

        /** The (pre-fetched) next element. */
        private @Nullable E next;

        /**
         * Creates a new instance.
         *
         * @param backing The backing iterator.
         */
        public Wrapper( final Iterator<E> backing ) {

            this.backing = backing;
            this.next = backing.hasNext() ? backing.next() : null;

        }

        @Override
        public E next() throws NoSuchElementException {

            if ( next != null ) {
                final var res = next;
                next = backing.hasNext() ? backing.next() : null;
                return res;
            } else {
                throw new NoSuchElementException( NO_MORE_ELEMENTS_ERROR );
            }

        }

        @Override
        public @Nullable E peek() {

            return next;

        }

    }

    /**
     * A smart iterator that iterates over a list.
     *
     * <p>This class relies on {@link Wrapper} with an iterator provided by the list for most 
     * of its operations, and thus has the same caveats; it only has changes necessary for 
     * supporting the {@link Detachable} interface.
     *
     * @param <E> The element type.
     * @since 1.0
     */
    class ListIterator<E extends @NonNull Object> extends Wrapper<E> implements Detachable<E> {

        /** The list being iterated over. */
        private final List<E> list;

        /** The index of the next element to be returned. */
        private int idx;

        /**
         * Creates a new instance.
         *
         * @param list The list to iterate over.
         */
        public ListIterator( final List<E> list ) {

            super( list.iterator() );
            this.list = list;
            this.idx = 0;

        }

        /**
         * Returns a list view over the portion of the list that has not been
         * iterated over yet.
         *
         * @return The remaining items as a list.
         */
        @SideEffectFree
        private List<E> remaining() {

            return list.subList( idx, list.size() );

        }

        @Override
        public E next() throws NoSuchElementException {

            idx++;
            return super.next();

        }

        @Override
        public Detachable<E> toIterator() {

            return new ListIterator<>( remaining() );

        }

        @Override
        public Spliterator<E> toSpliterator() {

            return remaining().spliterator();

        }

        @Override
        public Stream<E> toStream() {

            return remaining().stream();

        }

        @Override
        public Flux<E> toFlux() {

            return Flux.fromIterable( remaining() );

        }

    }

}
