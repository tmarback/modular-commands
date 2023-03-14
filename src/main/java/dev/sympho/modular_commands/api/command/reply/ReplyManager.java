package dev.sympho.modular_commands.api.command.reply;

import java.util.Arrays;

import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;
import discord4j.core.spec.InteractionFollowupCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import reactor.core.publisher.Mono;

/**
 * Manages the replies sent by a command.
 * 
 * <p>Note that if a reply specification has the {@link CommandReplySpec#privately()} field as
 * absent, the default for the command will be used. Furthermore, if the initial reply of the
 * command is {@link dev.sympho.modular_commands.api.command.Command#deferReply() deferred},
 * the value of that field on the first reply is ignored and the default is used instead
 * (further replies are unaffected).
 *
 * @version 1.0
 * @since 1.0
 * @implSpec Implementations are required to be concurrency-safe.
 */
public interface ReplyManager {

    /** Error message used when trying to access the initial reply before sending one. */
    String NO_RESPONSE_ERROR = "No response made yet.";

    /**
     * Sends a new reply.
     *
     * @param spec The reply specification.
     * @return The created reply.
     */
    Mono<Reply> add( CommandReplySpec spec );

    /**
     * Sends a new reply.
     *
     * @return A reply builder Mono that can be configured with the target reply then subscribed
     *         to send the reply.
     */
    default CommandReplyMono add() {

        return CommandReplyMono.of( this );

    }
    
    /**
     * Sends a new reply.
     *
     * @param spec The message specification.
     * @return The created reply.
     * @see ReplySpec#from(MessageCreateSpec)
     * @see #add(CommandReplySpec)
     */
    default Mono<Reply> add( final MessageCreateSpec spec ) {

        return add( CommandReplySpecGenerator.from( spec ) );

    }

    /**
     * Sends a new reply.
     *
     * @param spec The message specification.
     * @return The created reply.
     * @see ReplySpec#from(InteractionApplicationCommandCallbackSpec)
     * @see #add(CommandReplySpec)
     */
    default Mono<Reply> add( final InteractionApplicationCommandCallbackSpec spec ) {

        return add( CommandReplySpecGenerator.from( spec ) );

    }

    /**
     * Sends a new reply.
     *
     * @param spec The message specification.
     * @return The created reply.
     * @see ReplySpec#from(InteractionFollowupCreateSpec)
     * @see #add(CommandReplySpec)
     */
    default Mono<Reply> add( final InteractionFollowupCreateSpec spec ) {

        return add( CommandReplySpecGenerator.from( spec ) );

    }

    /**
     * Sends a new reply.
     *
     * @param content The message content.
     * @return The created reply.
     */
    default Mono<Reply> add( final String content ) {

        final var spec = CommandReplySpec.builder()
                .content( content )
                .build();
        return add( spec );

    }

    /**
     * Sends a new reply.
     *
     * @param embeds The message embeds.
     * @return The created reply.
     */
    default Mono<Reply> add( final EmbedCreateSpec... embeds ) {

        final var spec = CommandReplySpec.builder()
                .embeds( Arrays.asList( embeds ) )
                .build();
        return add( spec );

    }

    /**
     * Retrieves a reply.
     *
     * @param index The reply index.
     * @return The reply.
     * @throws IndexOutOfBoundsException if there is no reply with that index.
     */
    Reply get( int index ) throws IndexOutOfBoundsException;

    /**
     * Retrieves the initial reply.
     *
     * @return The reply.
     * @throws IllegalStateException if an initial reply was not sent yet.
     */
    default Reply get() throws IllegalStateException {

        try {
            return get( 0 );
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
