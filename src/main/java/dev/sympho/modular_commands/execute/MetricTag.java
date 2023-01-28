package dev.sympho.modular_commands.execute;

import java.util.Objects;
import java.util.Optional;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.Invocation;
import discord4j.common.util.Snowflake;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Tags used in metrics.
 *
 * @version 1.0
 * @since 1.0
 */
public interface MetricTag {

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
    enum Type implements MetricTag {

        /** A message-based command. */
        MESSAGE,
        
        /** A slash command-based command. */
        SLASH;

        @Override
        public String key() {
            return "command.type";
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
    ) implements MetricTag {

        /** The tag value for a direct message. */
        public static final String DM_VALUE = "none";

        /** 
         * The tag for a direct message (where there is no guild).
         * In this case, the value of the tag is {@value #DM_VALUE}.
         */
        public static final Guild DM = new Guild( null );

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
            return "command.guild";
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
    ) implements MetricTag {

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
            return "command.channel";
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
    ) implements MetricTag {

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
            return "command.caller";
        }

        @Override
        public String value() {
            return id.asString();
        }

    }

    /** 
     * The tag that indicates the invoked command.
     *
     * @param invocation The invocation.
     * @since 1.0
     */
    record Command(
            Invocation invocation
    ) implements MetricTag {

        /**
         * Creates a new instance.
         *
         * @param invocation The invocation.
         */
        public Command( final Invocation invocation ) {
            
            this.invocation = Objects.requireNonNull( invocation );

        }

        /**
         * Creates a tag with the given invocation as value.
         *
         * @param invocation The invocation.
         * @return The created tag.
         */
        public static Command from( final Invocation invocation ) {
            return new Command( invocation );
        }

        @Override
        public String key() {
            return "command.invocation";
        }

        @Override
        public String value() {
            return String.join( ",", invocation );
        }

    }
    
}
