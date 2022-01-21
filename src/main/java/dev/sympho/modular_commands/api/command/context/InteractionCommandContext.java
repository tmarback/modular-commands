package dev.sympho.modular_commands.api.command.context;

import org.checkerframework.dataflow.qual.Pure;

import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.interaction.ApplicationCommandInteractionEvent;

/**
 * The execution context of an command invoked through an interaction (application command).
 *
 * @version 1.0
 * @since 1.0
 */
public sealed interface InteractionCommandContext extends CommandContext
        permits SlashCommandContext {

    /**
     * Same as {@link #getEvent()}. This method only exists because
     * {@link AnyCommandContext} needs to exist.
     *
     * @return The trigger event.
     */
    @Pure
    ApplicationCommandInteractionEvent getInteractionEvent();

    @Override
    default Event getEvent() {
        return getInteractionEvent();
    }
    
}
