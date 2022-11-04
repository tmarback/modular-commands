package dev.sympho.modular_commands.utils;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

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

    @Override
    default List<String> apply( String raw ) {
        return split( raw );
    }

    /**
     * A splitter that is capable of processing the split in an asynchoronous manner.
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

            return Flux.generate( () -> raw, ( state, sink ) -> takeNext( state, sink::next ) );

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
