package dev.sympho.modular_commands.api.command.context;

import org.checkerframework.dataflow.qual.Pure;

import discord4j.core.event.domain.interaction.ApplicationCommandInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;

/**
 * The execution context of an command invoked through a slash command.
 *
 * @version 1.0
 * @since 1.0
 */
public interface SlashCommandContext extends InteractionCommandContext {

    /**
     * Same as {@link #getInteractionEvent()}. This method only exists because
     * {@link AnyCommandContext} needs to exist.
     *
     * @return The trigger event.
     */
    @Pure
    ChatInputInteractionEvent getSlashEvent();

    @Override
    default ApplicationCommandInteractionEvent getInteractionEvent() {
        return getSlashEvent();
    }
    
}
