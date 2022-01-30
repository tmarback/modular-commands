package dev.sympho.modular_commands.utils.builder.command;

import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.SlashCommand;
import dev.sympho.modular_commands.api.command.handler.SlashCommandHandler;
import dev.sympho.modular_commands.api.command.handler.SlashResultHandler;
import dev.sympho.modular_commands.impl.command.SlashCommandImpl;

/**
 * A builder for a slash command.
 *
 * @version 1.0
 * @since 1.0
 */
public final class SlashCommandBuilder extends CommandBuilder<SlashCommand, SlashCommandHandler, 
        SlashResultHandler, SlashCommandBuilder> {

    /**
     * Constructs a new builder with default values.
     */
    @SideEffectFree
    public SlashCommandBuilder() {}

    /**
     * Constructs a new builder that is a copy of the given builder.
     *
     * @param base The builder to copy.
    */
    @SideEffectFree
    public SlashCommandBuilder( final SlashCommandBuilder base ) {
        super( base );
    }

    /**
     * Constructs a new builder that is initialized to make a copy of 
     * the given command.
     *
     * @param base The command to copy.
     * @throws IllegalArgumentException if the given command has invalid values.
     */
    @SideEffectFree
    public SlashCommandBuilder( final SlashCommand base ) throws IllegalArgumentException {
        super( base );
    }

    @Override
    public SlashCommand build() throws IllegalStateException {

        try {
            return new SlashCommandImpl( 
                parent, buildName(), buildDisplayName(), buildDescription(), parameters,
                requiredDiscordPermissions, requireParentPermissions, 
                nsfw, botOwnerOnly, serverOwnerOnly, privateReply, 
                inheritSettings, buildInvocationHandler(), resultHandlers );
        } catch ( final IllegalArgumentException e ) {
            throw new IllegalStateException( "Invalid parameter configuration.", e );
        }

    }
    
}
