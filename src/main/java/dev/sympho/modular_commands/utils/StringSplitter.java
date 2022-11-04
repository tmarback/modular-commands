package dev.sympho.modular_commands.utils;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

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
     * A splitter that uses a shell-like splitting algorithm, where components are separated
     * by spaces, with the option of one or more components being delimited by quotes (single
     * or double) to allow for the inclusion of spaces.
     *
     * @version 1.0
     * @since 1.0
     */
    class Shell implements StringSplitter {

        /** Creates a new instance. */
        public Shell() {}

        /**
         * Finds the index of the next whitespace character in the given string.
         *
         * @param message The message to look into.
         * @return The index of the first whitespace, or -1 if none were found.
         * @implSpec This method does not consider extended Unicode.
         */
        @Pure
        private static int nextWhitespace( final String message ) {

            for ( int i = 0; i < message.length(); i++ ) {
                if ( Character.isWhitespace( message.charAt( i ) ) ) {
                    return i;
                }
            }
            return -1;
            
        }

        /**
         * Finds the index of the next closing delimiter character in the given message.
         * 
         * <p>A closing delimiter must be followed by either a space or the end of the
         * message. The first character is ignored, as it is assumed to be the opening
         * delimiter.
         *
         * @param message The message to look into.
         * @param delim The delimiter character to look for.
         * @return The index of the delimiter, or -1 if one was not found.
         * @implSpec This method does not consider extended Unicode.
         */
        @Pure
        private static int nextClose( final String message, final Character delim ) {

            int cur = 1;
            while ( cur >= 0 ) {

                cur = message.indexOf( delim, cur );
                if ( cur >= 0 && ( cur == message.length() - 1
                        || Character.isWhitespace( message.charAt( cur + 1 ) ) ) ) {
                    return cur;
                }

            }
            return -1;

        }

        /**
         * Parses the next arg from the message.
         * 
         * <p>Args are delimited by whitespace characters, unless enclosed by quotes (single
         * or double).
         *
         * @param message The message to parse.
         * @return A tuple with the next arg, and the remainder of the message (in that
         *         order).
         * @implSpec This method does not consider extended Unicode.
         */
        @SideEffectFree
        private static Tuple2<String, String> nextArg( final String message ) {

            int startIdx = 1;
            int endIdx;
            final int nextStart;

            if ( message.startsWith( "\"" ) ) {
                endIdx = nextClose( message, '"' );
            } else if ( message.startsWith( "'" ) ) {
                endIdx = nextClose( message, '\'' );
            } else {
                startIdx = 0;
                endIdx = nextWhitespace( message );
            }

            if ( endIdx < 0 ) {
                startIdx = 0;
                endIdx = message.length();
                nextStart = message.length();
            } else {
                nextStart = endIdx + 1;
            }

            return Tuples.of( message.substring( startIdx, endIdx ),
                    message.substring( nextStart ).trim() );

        }
    
        @Override
        public List<String> split( String raw ) {

            raw = raw.trim();
            final List<String> args = new LinkedList<>();

            while ( !raw.isEmpty() ) {

                final var next = nextArg( raw );

                args.add( next.getT1() );
                raw = next.getT2();

            }
    
            return List.copyOf( args );
    
        }

    }
    
}
