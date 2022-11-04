package dev.sympho.modular_commands.api.command.context;

import org.checkerframework.dataflow.qual.Pure;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;

/**
 * The execution context of an command invoked through a message.
 *
 * @version 1.0
 * @since 1.0
 */
public interface MessageCommandContext extends CommandContext {

    /**
     * Same as {@link #getEvent()}. This method only exists because
     * {@link AnyCommandContext} needs to exist.
     *
     * @return The trigger event.
     */
    @Pure
    MessageCreateEvent getMessageEvent();

    @Override
    default Event getEvent() {
        return getMessageEvent();
    }

    /**
     * Retrieves the message that invoked the command.
     *
     * @return The invoking message.
     */
    @Pure
    default Message getMessage() {
        return getMessageEvent().getMessage();
    }

    /**
     * Retrieves the ID of the message that invoked the command.
     *
     * @return The invoking message ID.
     */
    @Pure
    default Snowflake getMessageId() {
        return getMessage().getId();
    }
    
}
