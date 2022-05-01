package dev.sympho.modular_commands.api.command;

import java.time.Duration;
import java.util.Arrays;

import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.core.spec.MessageEditSpec;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

/**
 * Manages the replies sent by a command.
 *
 * @version 1.0
 * @since 1.0
 */
public interface ReplyManager {

    /**
     * Specifies a type of emphemeral response.
     *
     * @version 1.0
     * @since 1.0
     */
    enum EphemeralType {

        /** The message is not ephemeral. */
        NONE,

        /** The message is deleted after a delay. */
        TIMED,

        /** 
         * The message uses an ephemeral interaction response, if supported.
         * Has no effect if not an interaction.
         */
        INTERACTION,

        /** Both ephemeral types. */
        BOTH;

        /**
         * Whether the message should be deleted after a delay.
         *
         * @return {@code true} if the message should be deleted after a delay.
         */
        public boolean timed() {
            return this == TIMED || this == BOTH;
        }

        /**
         * Whether the message should use an ephemeral interaction response.
         *
         * @return {@code true} if the message should use an ephemeral interaction response.
         */
        public boolean interaction() {
            return this == INTERACTION || this == BOTH;
        }

    }

    /** Error message used when trying to access the initial reply before sending one. */
    String NO_RESPONSE_ERROR = "No response made yet.";

    /**
     * Sets whether <i>new</i> replies should be sent in DMs.
     * 
     * <p>Existing replies are not affected.
     *
     * @param priv If new replies should be sent in a private channel.
     * @return This.
     */
    ReplyManager setPrivate( boolean priv );

    /**
     * Sets whether <i>new</i> replies should be ephemeral.
     * 
     * <p>Existing replies are not affected.
     *
     * @param ephemeral The ephemeral type for new replies.
     * @return This.
     */
    ReplyManager setEphemeral( EphemeralType ephemeral );

    /**
     * Sets the delay before deleting <i>new</i> replies, if they are 
     * {@link EphemeralType#TIMED timed}.
     * 
     * <p>Existing replies are not affected.
     *
     * @param delay The delay.
     * @return This.
     */
    ReplyManager setDeleteDelay( Duration delay );

    /**
     * Defers initial response, indicating to the user that the response is being processed.
     *
     * @return A Mono that completes after deferral is processed.
     */
    Mono<Void> defer();

    /**
     * Sends an initial reply.
     *
     * @param spec The message specification.
     * @return A Mono that completes after the reply is sent.
     * @throws IllegalStateException if an initial reply was already made.
     */
    Mono<Void> reply( MessageCreateSpec spec ) throws IllegalStateException;

    /**
     * Sends an initial reply.
     *
     * @param content The message content.
     * @return A Mono that completes after the reply is sent.
     * @throws IllegalStateException if an initial reply was already made.
     */
    default Mono<Void> reply( final String content ) throws IllegalStateException {

        final var spec = MessageCreateSpec.builder()
                .content( content )
                .build();
        return reply( spec );

    }

    /**
     * Sends an initial reply.
     *
     * @param embeds The message embeds.
     * @return A Mono that completes after the reply is sent.
     * @throws IllegalStateException if an initial reply was already made.
     */
    default Mono<Void> reply( final EmbedCreateSpec... embeds ) throws IllegalStateException {

        final var spec = MessageCreateSpec.builder()
                .embeds( Arrays.asList( embeds ) )
                .build();
        return reply( spec );

    }

    /**
     * Adds a reply to the chain (either initial or followup).
     *
     * @param spec The message specification.
     * @return The created message and its index.
     */
    Mono<Tuple2<Message, Integer>> add( MessageCreateSpec spec );

    /**
     * Adds a reply to the chain (either initial or followup).
     *
     * @param content The message content.
     * @return The created message and its index.
     */
    default Mono<Tuple2<Message, Integer>> add( final String content ) {

        final var spec = MessageCreateSpec.builder()
                .content( content )
                .build();
        return add( spec );

    }

    /**
     * Adds a reply to the chain (either initial or followup).
     *
     * @param embeds The message embeds.
     * @return The created message and its index.
     */
    default Mono<Tuple2<Message, Integer>> add( final EmbedCreateSpec... embeds ) {

        final var spec = MessageCreateSpec.builder()
                .embeds( Arrays.asList( embeds ) )
                .build();
        return add( spec );

    }

    /**
     * Edits the initial reply.
     *
     * @param spec The edit specification.
     * @return The edited message. Fails with an {@link IllegalStateException} if the initial
     *         reply was deleted.
     * @throws IllegalStateException if an initial reply was not sent yet.
     */
    default Mono<Message> edit( final MessageEditSpec spec ) throws IllegalStateException {

        try {
            return edit( 0, spec );
        } catch ( final IndexOutOfBoundsException e ) {
            throw new IllegalStateException( NO_RESPONSE_ERROR );
        }

    }

    /**
     * Edits the initial reply.
     *
     * @param content The message content.
     * @return The edited message. Fails with an {@link IllegalStateException} if the initial
     *         reply was deleted.
     * @throws IllegalStateException if an initial reply was not sent yet.
     */
    default Mono<Message> edit( final String content ) throws IllegalStateException {

        final var spec = MessageEditSpec.builder()
                .contentOrNull( content )
                .build();
        return edit( spec );

    }

    /**
     * Edits the initial reply.
     *
     * @param embeds The message embeds.
     * @return The edited message. Fails with an {@link IllegalStateException} if the initial
     *         reply was deleted.
     * @throws IllegalStateException if an initial reply was not sent yet.
     */
    default Mono<Message> edit( final EmbedCreateSpec... embeds ) throws IllegalStateException {

        final var spec = MessageEditSpec.builder()
                .embeds( Arrays.asList( embeds ) )
                .build();
        return edit( spec );

    }

    /**
     * Edits a reply.
     *
     * @param index The reply index.
     * @param spec The edit specification.
     * @return The edited message. Fails with an {@link IllegalStateException} if the initial
     *         reply was deleted.
     * @throws IndexOutOfBoundsException if there is no reply with that index.
     */
    Mono<Message> edit( int index, MessageEditSpec spec ) throws IndexOutOfBoundsException;

    /**
     * Edits a reply.
     *
     * @param index The reply index.
     * @param content The message content.
     * @return The edited message. Fails with an {@link IllegalStateException} if the initial
     *         reply was deleted.
     * @throws IndexOutOfBoundsException if there is no reply with that index.
     */
    default Mono<Message> edit( final int index, final String content ) 
            throws IndexOutOfBoundsException {

        final var spec = MessageEditSpec.builder()
                .contentOrNull( content )
                .build();
        return edit( index, spec );

    }

    /**
     * Edits a reply.
     *
     * @param index The reply index.
     * @param embeds The message embeds.
     * @return The edited message. Fails with an {@link IllegalStateException} if the initial
     *         reply was deleted.
     * @throws IndexOutOfBoundsException if there is no reply with that index.
     */
    default Mono<Message> edit( final int index, final EmbedCreateSpec... embeds ) 
            throws IndexOutOfBoundsException {

        final var spec = MessageEditSpec.builder()
                .embeds( Arrays.asList( embeds ) )
                .build();
        return edit( index, spec );

    }

    /**
     * Retrieves a reply.
     *
     * @param index The reply index.
     * @return The reply. Fails with an {@link IllegalStateException} if the
     *         reply was deleted.
     * @throws IndexOutOfBoundsException if there is no reply with that index.
     */
    Mono<Message> get( int index ) throws IndexOutOfBoundsException;

    /**
     * Retrieves the initial reply.
     *
     * @return The reply. Fails with an {@link IllegalStateException} if the initial
     *         reply was deleted.
     * @throws IllegalStateException if an initial reply was not sent yet.
     */
    default Mono<Message> get() throws IllegalStateException {

        try {
            return get( 0 );
        } catch ( final IndexOutOfBoundsException e ) {
            throw new IllegalStateException( NO_RESPONSE_ERROR );
        }

    }

    /**
     * Deletes a reply.
     * 
     * <p>Note that this does not change the index of other replies within this manager.
     *
     * @param index The reply index.
     * @return A Mono that completes when the reply is deleted. Fails with an
     *         {@link IllegalStateException} if the reply was already deleted.
     * @throws IndexOutOfBoundsException if there is no reply with that index.
     */
    Mono<Void> delete( int index ) throws IndexOutOfBoundsException;

    /**
     * Deletes the initial reply.
     * 
     * <p>Note that this does not change the index of other replies within this manager.
     *
     * @return A Mono that completes when the reply is deleted. Fails with an
     *         {@link IllegalStateException} if the initial reply was already deleted.
     * @throws IllegalStateException if an initial reply was not sent yet.
     */
    default Mono<Void> delete() throws IllegalStateException {

        try {
            return delete( 0 );
        } catch ( final IndexOutOfBoundsException e ) {
            throw new IllegalStateException( NO_RESPONSE_ERROR );
        }

    }

    /**
     * Obtains a manager that is a continuation of this one and has the same configuration,
     * but is guaranteed to continue working long-term by using alternate sending methods
     * if necessary (for example, it might use regular messages instead of interaction
     * replies).
     * 
     * <p>Note that, while the existing replies are still <i>accessible</i> (by using 
     * {@link #get(int)}), editing them may not be possible (for example, an 
     * interaction-ephemeral response).
     * 
     * <p>There is no guarantee that the returned manager is independent from this one;
     * using this manager after calling this method causes undefined behavior.
     *
     * @return The long-term manager.
     */
    ReplyManager longTerm();
    
}
