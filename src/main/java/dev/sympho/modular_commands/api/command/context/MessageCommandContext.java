package dev.sympho.modular_commands.api.command.context;

import org.checkerframework.dataflow.qual.Pure;

import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.message.MessageCreateEvent;

/**
 * The execution context of an command invoked through a message.
 *
 * @version 1.0
 * @since 1.0
 */
public non-sealed interface MessageCommandContext extends CommandContext {

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
    
}
