package dev.sympho.modular_commands.api.command.context;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;

/**
 * The execution context of an command invoked through a slash command.
 *
 * @version 1.0
 * @since 1.0
 */
public interface SlashCommandContext extends InteractionCommandContext {

    @Override
    ChatInputInteractionEvent getEvent();
    
}
