package dev.sympho.modular_commands.api.command.context;

import discord4j.core.event.domain.message.MessageCreateEvent;

/**
 * The execution context of an command invoked through a message.
 *
 * @version 1.0
 * @since 1.0
 */
public interface MessageCommandContext extends CommandContext {

    @Override
    MessageCreateEvent getEvent();
    
}
