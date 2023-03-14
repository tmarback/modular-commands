package dev.sympho.modular_commands.api.command.reply;

import org.checkerframework.dataflow.qual.SideEffectFree;

import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;
import discord4j.core.spec.InteractionFollowupCreateSpec;
import discord4j.core.spec.InteractionReplyEditSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.core.spec.MessageEditSpec;

/**
 * Utilities for creating reply specification instances.
 *
 * @version 1.0
 * @since 1.0
 */
public final class ReplySpec {

    /** Do not instantiate. */
    private ReplySpec() {}

    /**
     * Creates a reply spec from a message create spec.
     * 
     * <p>The {@link CommandReplySpec#privately()} field is set to absent.
     *
     * @param spec The source spec.
     * @return The converted spec.
     */
    @SideEffectFree
    static CommandReplySpec from( final MessageCreateSpec spec ) {

        return CommandReplySpecGenerator.from( spec );

    }

    /**
     * Creates a reply spec from an application command reply spec.
     * 
     * <p>The {@link CommandReplySpec#privately()} field is set to 
     * {@link InteractionApplicationCommandCallbackSpec#ephemeral()}.
     *
     * @param spec The source spec.
     * @return The converted spec.
     */
    @SideEffectFree
    static CommandReplySpec from( final InteractionApplicationCommandCallbackSpec spec ) {

        return CommandReplySpecGenerator.from( spec );

    }

    /**
     * Creates a reply spec from an application command followup spec.
     * 
     * <p>The {@link CommandReplySpec#privately()} field is set to 
     * {@link InteractionFollowupCreateSpec#ephemeral()}.
     *
     * @param spec The source spec.
     * @return The converted spec.
     */
    @SideEffectFree
    static CommandReplySpec from( final InteractionFollowupCreateSpec spec ) {

        return CommandReplySpecGenerator.from( spec );

    }

    /**
     * Creates an edit spec from a message edit spec.
     *
     * @param spec The source spec.
     * @return The converted spec.
     */
    @SideEffectFree
    static CommandReplyEditSpec from( final MessageEditSpec spec ) {

        return CommandReplyEditSpecGenerator.from( spec );

    }

    /**
     * Creates an edit spec from an interaction reply edit spec.
     *
     * @param spec The source spec.
     * @return The converted spec.
     */
    @SideEffectFree
    static CommandReplyEditSpec from( final InteractionReplyEditSpec spec ) {

        return CommandReplyEditSpecGenerator.from( spec );

    }
    
}
