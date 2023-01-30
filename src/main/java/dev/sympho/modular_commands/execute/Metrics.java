package dev.sympho.modular_commands.execute;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.collections4.ListUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.Invocation;
import discord4j.common.util.Snowflake;
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
         * The tag that indicates the guild that the command was called from, if any.
         *
         * @param id The guild ID, or {@code null} if called from a private channel.
         * @since 1.0
         */
        record Guild(
                @Nullable Snowflake id
        ) implements Tag {

            /** The tag value for a direct message. */
            public static final String DM_VALUE = "none";

            /** 
             * The tag for a direct message (where there is no guild).
             * In this case, the value of the tag is {@value #DM_VALUE}.
             */
            public static final Guild DM = new Guild( null );

            /** The tag name. */
            private static final String NAME = Metrics.name( "guild" );

            /**
             * Creates a tag with the given id as value.
             *
             * @param id The ID of the guild. May be {@code null} for a private channel.
             * @return The created tag.
             */
            public static Guild from( final @Nullable Snowflake id ) {
                return id == null ? DM : new Guild( id );
            }

            /**
             * Creates a tag with the given id as value.
             *
             * @param id The ID of the guild. May be empty for a private channel.
             * @return The created tag.
             */
            @SuppressWarnings( "optional.parameter" ) // It's for compatibility
            public static Guild from( final Optional<Snowflake> id ) {
                return id.map( Guild::new ).orElse( DM );
            }

            @Override
            public String key() {
                return NAME;
            }

            @Override
            public String value() {
                return id == null ? DM_VALUE : id.asString();
            }

        }

        /** 
         * The tag that indicates the channel that the command was called from.
         *
         * @param id The channel ID.
         * @since 1.0
         */
        record Channel(
                Snowflake id
        ) implements Tag {

            /** The tag name. */
            private static final String NAME = Metrics.name( "channel" );

            /**
             * Creates a new instance.
             *
             * @param id The channel ID.
             */
            public Channel( final Snowflake id ) {
                
                this.id = Objects.requireNonNull( id );

            }

            /**
             * Creates a tag with the given id as value.
             *
             * @param id The ID of the channel.
             * @return The created tag.
             */
            public static Channel from( final Snowflake id ) {
                return new Channel( id );
            }

            @Override
            public String key() {
                return NAME;
            }

            @Override
            public String value() {
                return id.asString();
            }

        }

        /** 
         * The tag that indicates the user who invoked the command.
         *
         * @param id The user ID.
         * @since 1.0
         */
        record Caller(
                Snowflake id
        ) implements Tag {

            /** The tag name. */
            private static final String NAME = Metrics.name( "caller" );

            /**
             * Creates a new instance.
             *
             * @param id The caller ID.
             */
            public Caller( final Snowflake id ) {
                
                this.id = Objects.requireNonNull( id );

            }

            /**
             * Creates a tag with the given id as value.
             *
             * @param id The ID of the caller.
             * @return The created tag.
             */
            public static Caller from( final Snowflake id ) {
                return new Caller( id );
            }

            @Override
            public String key() {
                return NAME;
            }

            @Override
            public String value() {
                return id.asString();
            }

        }

        /** 
         * The tag that indicates the command invocation.
         *
         * @param invocation The invocation.
         * @param canonical {@code true} if the invocation is the canonical one 
         *                  (with aliases resolved).
         *                  {@code false} if the invocation is the one called by user 
         *                  (may have aliases).
         * @since 1.0
         */
        record CommandInvocation(
                Invocation invocation,
                boolean canonical
        ) implements Tag {

            /** The prefix for the tag names. */
            public static final String NAME_PREFIX = "invocation";

            /** The tag name for a called invocation. */
            private static final String NAME_CALLED = Metrics.name( NAME_PREFIX );

            /** The tag name for a canonical invocation. */
            private static final String NAME_CANONICAL = Metrics.name( NAME_PREFIX, "canonical" );

            /**
             * Creates a new instance.
             *
             * @param invocation The invocation.
             * @param canonical {@code true} if the invocation is the canonical one 
             *                  (with aliases resolved).
             *                  {@code false} if the invocation is the one called by user 
             *                  (may have aliases).
             */
            public CommandInvocation( final Invocation invocation, final boolean canonical ) {
                
                this.invocation = Objects.requireNonNull( invocation );
                this.canonical = canonical;

            }

            /**
             * Creates a tag with the given called invocation as value.
             *
             * @param invocation The invocation.
             * @return The created tag.
             */
            public static CommandInvocation called( final Invocation invocation ) {
                return new CommandInvocation( invocation, false );
            }

            /**
             * Creates a tag with the given canonical invocation as value.
             *
             * @param invocation The invocation.
             * @return The created tag.
             */
            public static CommandInvocation canonical( final Invocation invocation ) {
                return new CommandInvocation( invocation, true );
            }

            @Override
            public String key() {
                return canonical ? NAME_CANONICAL : NAME_CALLED;
            }

            @Override
            public String value() {
                return String.join( ",", invocation );
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
