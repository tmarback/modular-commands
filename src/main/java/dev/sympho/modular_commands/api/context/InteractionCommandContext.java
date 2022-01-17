package dev.sympho.modular_commands.api.context;

import discord4j.core.event.domain.interaction.ApplicationCommandInteractionEvent;

/**
 * The execution context of an command invoked through an interaction (application command).
 *
 * @version 1.0
 * @since 1.0
 */
public interface InteractionCommandContext extends CommandContext {

    @Override
    ApplicationCommandInteractionEvent getEvent();
    
}
