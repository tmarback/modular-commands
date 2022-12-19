package dev.sympho.modular_commands.utils;

import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.utils.SmartIterator.ListIterator;
import reactor.core.publisher.Flux;

/**
 * A function that splits a string into multiple components.
 *
 * @version 1.0
 * @since 1.0
 */
public interface StringSplitter extends Function<String, List<String>> {

    /**
     * Splits the given string into components.
     *
     * @param raw The string to split.
     * @return The split components.
     */
    @SideEffectFree
    List<String> split( String raw );

    /**
     * Obtains a string that can be used to delimit two elements according to
     * this splitter.
     * 
     * <p>Note that there is no guarantee that the value returned by this method is the
     * <i>only</i> delimiter supported by this splitter, only that it is <i>a</i> valid
     * delimiter.
     * 
     * <p>In other words, the guarantee offered by this method is that, given some string
     * {@code str} and splitter {@code spl}, and {@code l = spl.split(str)}, then
     * {@code spl.split(String.join(spl.delimiter(), l)).equals(l)} is {@code true}.
     * 
     * <p>This implies that, for any sequence of strings {@code s1, s2, s3, ...}, then
     * {@code spl.split(String.join(spl.delimiter(), s1, s2, s3, ...))}
     * gives the same result as concatenating
     * {@code spl.split(s1), spl.split(s2), spl.split(s3), ...}
     *
     * @return The delimiter.
     * @apiNote This method 
     */
    @Pure
    String delimiter();

    @Override
    default List<String> apply( String raw ) {
        return split( raw );
    }

    /**
     * Creates a smart iterator that iterates over the components obtained by splitting
     * the given raw string. The returned iterator is functionally equivalent to calling
     * {@link SmartIterator#from(List)} on the result of {@link #split(String)}, but
     * may (depending on implementation) split components lazily on demand.
     *
     * @param raw The string to split.
     * @return A smart iterator over the split components.
     * @implSpec The default implementation delegates to {@link #split(String)} and
     *           {@link SmartIterator#from(List)}.
     */
    @SideEffectFree
    default Iterator iterate( final String raw ) {

        /**
         * Base iterator implementation.
         *
         * @since 1.0
         */
        class BaseIterator extends ListIterator<String> implements Iterator {

            /**
             * Creates a new iterator with the given parsed result.
             *
             * @param parsed The parsing result.
             */
            BaseIterator( final List<String> parsed ) {

                super( parsed );

            }

            @Override
            public StringSplitter splitter() {

                return StringSplitter.this;

            }

            @Override
            public Iterator toIterator() {

                return new BaseIterator( remaining() );

            }

        }

        return new BaseIterator( split( raw ) );
    }

    /**
     * Creates an empty iterator for this splitter.
     *
     * @return The iterator.
     */
    @SideEffectFree
    default Iterator emptyIterator() {

        return new EmptyIterators.EmptySplitter<>( this );

    }

    /**
     * An iterator over the elements split by a splitter.
     * 
     * <p>May, depending on implementation, split components lazily on demand.
     *
     * @since 1.0
     */
    interface Iterator extends SmartIterator.Detachable<String> {

        /**
         * Retrieves the splitter that created this iterator.
         *
         * @return The source splitter.
         */
        @Pure
        StringSplitter splitter();

        @Override
        Iterator toIterator();

    }

    /**
     * A splitter that is capable of processing the split in an asynchronous manner.
     *
     * @version 1.0
     * @since 1.0
     */
    interface Async extends StringSplitter {

        /**
         * Takes the next element from the current state.
         *
         * @param state The current processing state.
         * @param sink The sink to send the next element into.
         * @return The new state.
         */
        String takeNext( String state, Consumer<String> sink );

        @Override
        default List<String> split( String raw ) {

            final List<String> components = new LinkedList<>();
            while ( !raw.isEmpty() ) {

                raw = takeNext( raw, components::add );

            }
            return List.copyOf( components );
    
        }

        /**
         * Splits the given string into components. Splitting is performed asynchronously
         * as requests are received from downstream.
         *
         * @param raw The string to split.
         * @return The split components.
         */
        @SideEffectFree
        default Flux<String> splitAsync( final String raw ) {

            return Flux.generate( () -> raw, ( state, sink ) -> {
                if ( state.isEmpty() ) {
                    sink.complete();
                    return "";
                } else {
                    return takeNext( state, sink::next );
                }
            } );

        }

        @Override
        default Iterator iterate( final String raw ) {

            return new Iterator() {

                /** The next value to be returned. */
                private final AtomicReference<@Nullable String> next;
                /** The current state (not traversed yet). */
                private String state;
                /** The next state (remaining to parse). */
                private String nextState;

                {
    
                    this.next = new AtomicReference<>();
    
                    state = raw;
                    nextState = takeNext( raw, next::set );
    
                }
    
                @Override
                public String remainder() {
                    return state;
                }
    
                @Override
                public String next() throws NoSuchElementException {
    
                    final var n = next.getAndSet( null );
                    if ( n == null ) {
                        throw new NoSuchElementException( "No more elements" );
                    }
    
                    state = nextState;
                    nextState = takeNext( state, next::set );
                    return n;
    
                }
    
                @Override
                public @Nullable String peek() {
                    return next.get();
                }
    
                @Override
                public Iterator toIterator() {
                    return iterate( state );
                }
    
                @Override
                public Spliterator<String> toSpliterator() {
                    return spliterate( state );
                }
    
                @Override
                public Stream<String> toStream() {
                    return splitStream( state );
                }
    
                @Override
                public Flux<String> toFlux() {
                    return splitAsync( state );
                }

                @Override
                public StringSplitter.Async splitter() {
                    return StringSplitter.Async.this;
                }
    
            };

        }

        /**
         * Creates a spliterator that iterates over the components obtained by splitting
         * the given raw string. The returned iterator is functionally equivalent to calling
         * {@link List#spliterator()} on the result of {@link #split(String)}, but splits 
         * components lazily on demand.
         *
         * @param raw The string to split.
         * @return A spliterator over the split components.
         */
        @SideEffectFree
        default Spliterator<String> spliterate( final String raw ) {

            return Spliterators.spliteratorUnknownSize( 
                    iterate( raw ), 
                    Spliterator.IMMUTABLE | Spliterator.NONNULL | Spliterator.ORDERED 
            );

        }

        /**
         * Creates a stream that contains over the components obtained by splitting
         * the given raw string. The returned iterator is functionally equivalent to calling
         * {@link List#stream()} on the result of {@link #split(String)}, but splits 
         * components lazily on demand.
         *
         * @param raw The string to split.
         * @return A stream of the split components.
         */
        @SideEffectFree
        default Stream<String> splitStream( final String raw ) {

            return StreamSupport.stream( spliterate( raw ), false );

        }

        /**
         * Creates an empty iterator for this splitter.
         *
         * @return The iterator.
         */
        @Override
        default Iterator emptyIterator() {

            return new EmptyIterators.EmptyAsyncSplitter<>( this );

        }

        /**
         * Iterator that splits elements lazily on demand during traversal.
         *
         * @since 1.0
         */
        interface Iterator extends StringSplitter.Iterator {

            /**
             * Retrieves the remainder of the string that has not been parsed yet.
             *
             * @return The remainder (not traversed) portion of the string.
             */
            @Pure
            String remainder();

            @Override
            Async splitter();

            @Override
            Iterator toIterator();

        }

    }

    /**
     * A splitter that uses a shell-like splitting algorithm, where components are separated
     * by spaces, with the option of one or more components being delimited by quotes (single
     * or double) to allow for the inclusion of spaces.
     * 
     * <p>Leading and trailing whitespace is ignored.
     *
     * @version 1.0
     * @since 1.0
     */
    class Shell implements Async {

        /** Creates a new instance. */
        public Shell() {}

        @Override
        public String delimiter() {
            return " ";
        }

        /**
         * Finds the index of the next whitespace character in the given string.
         *
         * @param value The string to look into.
         * @return The index of the first whitespace, or -1 if none were found.
         * @implSpec This method does not consider extended Unicode.
         */
        @Pure
        private static int nextWhitespace( final String value ) {
            
            for ( int i = 0; i < value.length(); i++ ) {
                if ( Character.isWhitespace( value.charAt( i ) ) ) {
                    return i;
                }
            }
            return -1;
            
        }

        /**
         * Finds the index of the next closing delimiter character in the given string.
         * 
         * <p>A closing delimiter must be followed by either a space or the end of the
         * message. The first character is ignored, as it is assumed to be the opening
         * delimiter.
         *
         * @param value The string to look into.
         * @param delim The delimiter character to look for.
         * @return The index of the delimiter, or -1 if one was not found.
         * @implSpec This method does not consider extended Unicode.
         */
        @Pure
        private static int nextClose( final String value, final Character delim ) {

            int cur = 1;
            while ( cur >= 0 ) {

                cur = value.indexOf( delim, cur );
                if ( cur >= 0 && ( cur == value.length() - 1
                        || Character.isWhitespace( value.charAt( cur + 1 ) ) ) ) {
                    return cur;
                }

            }
            return -1;

        }

        @Override
        public String takeNext( final String state, final Consumer<String> sink ) {

            final String current = state.trim();
            if ( current.isEmpty() ) {
                return current;
            }

            int startIdx = 1;
            int endIdx;
            final int nextStart;

            if ( current.startsWith( "\"" ) ) {
                endIdx = nextClose( current, '"' );
            } else if ( current.startsWith( "'" ) ) {
                endIdx = nextClose( current, '\'' );
            } else {
                startIdx = 0;
                endIdx = nextWhitespace( current );
            }

            if ( endIdx < 0 ) {
                startIdx = 0;
                endIdx = current.length();
                nextStart = current.length();
            } else {
                nextStart = endIdx + 1;
            }

            sink.accept( current.substring( startIdx, endIdx ) );
            return current.substring( nextStart ).trim();

        }

    }
    
}
