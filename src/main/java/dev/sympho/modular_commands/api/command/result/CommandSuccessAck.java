package dev.sympho.modular_commands.api.command.result;

import org.checkerframework.dataflow.qual.Pure;

import dev.sympho.modular_commands.api.command.reply.CommandReplySpec;
import dev.sympho.modular_commands.execute.BaseHandler;
import discord4j.core.object.reaction.ReactionEmoji;

/**
 * A successful result that should be acknowledged to the user.
 * 
 * <p>The {@link BaseHandler#DEFAULT default handler} handles this result by reacting to the
 * triggering message with the value given by {@link #react()}, if the command was triggered
 * by a message, otherwise {@link CommandReplySpec#privately() privately} responding with the
 * message given by {@link #message()}.
 *
 * @version 1.0
 * @since 1.0
 */
public interface CommandSuccessAck extends CommandSuccess {

    /**
     * The react to use if the command was triggered by a message.
     *
     * @return The react.
     */
    @Pure
    ReactionEmoji react();

    /**
     * The message to send if the command was not triggered by a message.
     *
     * @return The message.
     */
    @Pure
    String message();
    
}
