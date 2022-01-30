package dev.sympho.modular_commands.impl.command;

import java.util.List;

import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.InteractionCommand;
import dev.sympho.modular_commands.api.command.Invocation;
import dev.sympho.modular_commands.api.command.handler.InteractionCommandHandler;
import dev.sympho.modular_commands.api.command.handler.InteractionResultHandler;
import dev.sympho.modular_commands.api.command.parameter.Parameter;
import dev.sympho.modular_commands.utils.CommandUtils;
import discord4j.rest.util.PermissionSet;

/**
 * Default implementation of an interaction-based command.
 *
 * @param parent The parent of the command.
 * @param name The name of the command.
 * @param displayName The display name of the command.
 * @param description The description of the command.
 * @param parameters The command parameters, in the order that they should be provided
 *                   by the user.
 * @param requiredDiscordPermissions he built-in permissions that a user should have in
 *                                   order to execute the command.
 * @param requireParentPermissions Whether a user invoking this command must also have 
 *                                 the permissions to invoke its parent command.
 * @param nsfw Whether this command can only be invoked in a NSFW channel.
 * @param botOwnerOnly Whether this command can only be invoked by the owner of the bot.
 * @param serverOwnerOnly Whether this command can only be invoked by the owner of the server.
 * @param privateReply Whether this command's response is sent in a way that only the 
 *                     invoking user can see.
 * @param inheritSettings Whether the command settings should be inherited from the parent 
 *                        command (ignoring the values provided by this command).
 * @param invocationHandler The handler to use for processing an invocation of the command.
 * @param resultHandlers The handlers to use for processing the result, in order.
 * @version 1.0
 * @since 1.0
 */
public record InteractionCommandImpl(
        Invocation parent,
        String name,
        String displayName,
        String description,
        List<Parameter<?>> parameters,
        PermissionSet requiredDiscordPermissions,
        boolean requireParentPermissions,
        boolean nsfw,
        boolean botOwnerOnly,
        boolean serverOwnerOnly,
        boolean privateReply,
        boolean inheritSettings,
        InteractionCommandHandler invocationHandler,
        List<? extends InteractionResultHandler> resultHandlers
) implements InteractionCommand {

    /**
     * Initializes a new instance.
     *
     * @param parent The parent of the command.
     * @param name The name of the command.
     * @param displayName The display name of the command.
     * @param description The description of the command.
     * @param parameters The command parameters, in the order that they should be provided
     *                   by the user.
     * @param requiredDiscordPermissions he built-in permissions that a user should have in
     *                                   order to execute the command.
     * @param requireParentPermissions Whether a user invoking this command must also have 
     *                                 the permissions to invoke its parent command.
     * @param nsfw Whether this command can only be invoked in a NSFW channel.
     * @param botOwnerOnly Whether this command can only be invoked by the owner of the bot.
     * @param serverOwnerOnly Whether this command can only be invoked by the owner of the server.
     * @param privateReply Whether this command's response is sent in a way that only the 
     *                     invoking user can see.
     * @param inheritSettings Whether the command settings should be inherited from the parent 
     *                        command (ignoring the values provided by this command).
     * @param invocationHandler The handler to use for processing an invocation of the command.
     * @param resultHandlers The handlers to use for processing the result, in order.
     */
    @SideEffectFree
    @SuppressWarnings( "checkstyle:parameternumber" )
    public InteractionCommandImpl(
            final Invocation parent,
            final String name,
            final String displayName,
            final String description,
            final List<Parameter<?>> parameters,
            final PermissionSet requiredDiscordPermissions,
            final boolean requireParentPermissions,
            final boolean nsfw,
            final boolean botOwnerOnly,
            final boolean serverOwnerOnly,
            final boolean privateReply,
            final boolean inheritSettings,
            final InteractionCommandHandler invocationHandler,
            final List<? extends InteractionResultHandler> resultHandlers
    ) {

        this.parent = CommandUtils.validateParent( parent );
        this.name = CommandUtils.validateName( name );
        this.displayName = CommandUtils.validateDisplayName( displayName );
        this.description = CommandUtils.validateDescription( description );
        this.parameters = CommandUtils.validateParameters( parameters, true );
        this.requiredDiscordPermissions = CommandUtils.validateDiscordPermissions( 
                requiredDiscordPermissions );
        this.requireParentPermissions = requireParentPermissions;
        this.nsfw = nsfw;
        this.botOwnerOnly = botOwnerOnly;
        this.serverOwnerOnly = serverOwnerOnly;
        this.privateReply = privateReply;
        this.inheritSettings = inheritSettings;
        this.invocationHandler = CommandUtils.validateInvocationHandler( invocationHandler );
        this.resultHandlers = CommandUtils.validateResultHandlers( resultHandlers );

    }
    
}
