package dev.sympho.modular_commands.execute;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.apache.commons.collections4.ListUtils;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Configuration for metrics instrumentation.
 *
 * @version 1.0
 * @since 1.0
 */
public final class Metrics {

    /** The prefix for all metric and tag names. */
    public static final String PREFIX = "command";

    /** Do not instantiate. */
    private Metrics() {}

    /**
     * Creates the name for a metric or tag from a sequence of components. The 
     * {@link #PREFIX} is included.
     *
     * @param components The components to use for the name.
     * @return The metric/tag name.
     */
    //@StaticallyExecutable
    public static String name( final String... components ) {

        return String.join( ".", ListUtils.union(
                List.of( PREFIX ), 
                Arrays.asList( components ) 
        ) );

    }

    /**
     * Tags used in metrics.
     *
     * @since 1.0
     */
    public interface Tag {

        /**
         * Retrieves the tag key.
         *
         * @return The tag key.
         */
        @Pure
        String key();

        /**
         * Retrieves the tag value.
         *
         * @return The tag value.
         */
        @Pure
        String value();

        /**
         * Applies this tag to the given mono using {@link Mono#tag(String, String)}.
         *
         * @param <T> The mono value type.
         * @param mono The mono to apply the tag to.
         * @return The mono with the tag applied.
         */
        @SideEffectFree
        default <T> Mono<T> apply( final Mono<T> mono ) {

            return mono.tag( key(), value() );

        }

        /**
         * Applies this tag to the given flux using {@link Flux#tag(String, String)}.
         *
         * @param <T> The flux value type.
         * @param flux The flux to apply the tag to.
         * @return The flux with the tag applied.
         */
        @SideEffectFree
        default <T> Flux<T> apply( final Flux<T> flux ) {

            return flux.tag( key(), value() );

        }

        /* Common tags */

        /**
         * The tag that indicates the command type.
         *
         * @since 1.0
         */
        enum Type implements Tag {

            /** A message-based command. */
            MESSAGE,
            
            /** A slash command-based command. */
            SLASH;

            /** The tag name. */
            private static final String NAME = Metrics.name( "type" );

            @Override
            public String key() {
                return NAME;
            }
            
            @Override
            public String value() {
                return name().toLowerCase();
            }

        }

        /** 
         * The tag that indicates the command ID.
         *
         * @param id The command ID.
         * @since 1.0
         */
        record CommandId(
                String id
        ) implements Tag {

            /** The tag name. */
            private static final String NAME = Metrics.name( "id" );

            /**
             * Creates a new instance.
             *
             * @param id The command ID.
             */
            public CommandId( final String id ) {
                
                this.id = Objects.requireNonNull( id );

            }

            /**
             * Creates a tag with the given id as value.
             *
             * @param id The ID of the command.
             * @return The created tag.
             */
            public static CommandId from( final String id ) {
                return new CommandId( id );
            }

            @Override
            public String key() {
                return NAME;
            }

            @Override
            public String value() {
                return id;
            }

        }

    }
    
}
