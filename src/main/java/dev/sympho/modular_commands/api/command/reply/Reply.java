package dev.sympho.modular_commands.api.command.reply;

import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.InteractionReplyEditSpec;
import discord4j.core.spec.MessageEditSpec;
import reactor.core.publisher.Mono;

/**
 * A reply made by a command.
 *
 * @version 1.0
 * @since 1.0
 */
public interface Reply {

    /**
     * The index of the reply in the sequence of replies sent by the command.
     *
     * @return The index of the reply.
     * @see ReplyManager#get(int)
     */
    @Pure
    int index();

    /**
     * The ID of the reply message.
     *
     * @return The message ID.
     */
    @Pure
    Snowflake id();

    /**
     * Retrieves the reply message.
     *
     * @return The reply message.
     */
    @SideEffectFree
    Mono<Message> message();

    /**
     * Edits the reply message.
     *
     * @param spec The edit specification.
     * @return The edited message.
     */
    Mono<Message> edit( CommandReplyEditSpec spec );

    /**
     * Edits the reply message.
     *
     * @return An edit builder Mono that can be configured with the target edit then subscribed
     *         to apply the edit.
     */
    default CommandReplyEditMono edit() {

        return CommandReplyEditMono.of( this );

    }

    /**
     * Edits the reply message.
     *
     * @param spec The edit specification.
     * @return The edited message.
     * @see ReplySpec#from(MessageEditSpec)
     * @see #edit(CommandReplyEditSpec)
     */
    default Mono<Message> edit( final MessageEditSpec spec ) {

        return edit( ReplySpec.from( spec ) );

    }

    /**
     * Edits the reply message.
     *
     * @param spec The edit specification.
     * @return The edited message.
     * @see ReplySpec#from(InteractionReplyEditSpec)
     * @see #edit(CommandReplyEditSpec)
     */
    default Mono<Message> edit( final InteractionReplyEditSpec spec ) {

        return edit( ReplySpec.from( spec ) );

    }

    /**
     * Deletes the reply message.
     *
     * @return A mono that completes when the message has been deleted.
     */
    Mono<Void> delete();
    
}
