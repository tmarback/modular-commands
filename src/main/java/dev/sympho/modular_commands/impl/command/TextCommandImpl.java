package dev.sympho.modular_commands.impl.command;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.Invocation;
import dev.sympho.modular_commands.api.command.ReplyManager.EphemeralType;
import dev.sympho.modular_commands.api.command.TextCommand;
import dev.sympho.modular_commands.api.command.handler.AnyInvocationHandler;
import dev.sympho.modular_commands.api.command.handler.AnyResultHandler;
import dev.sympho.modular_commands.api.command.parameter.Parameter;
import dev.sympho.modular_commands.utils.CommandUtils;
import discord4j.rest.util.PermissionSet;

/**
 * Default implementation of a flexible command.
 *
 * @param scope The scope that the command is defined in.
 * @param callable If the command may be directly invoked by users.
 * @param parent The parent of the command.
 * @param name The name of the command.
 * @param displayName The display name of the command.
 * @param aliases The aliases that may also invoke the command.
 * @param description The description of the command.
 * @param parameters The command parameters, in the order that they should be provided
 *                   by the user.
 * @param requiredPermissions The permissions that a user should have in order to execute 
 *                            the command.
 * @param requireParentPermissions Whether a user invoking this command must also have 
 *                                 the permissions to invoke its parent command.
 * @param nsfw Whether this command can only be invoked in a NSFW channel.
 * @param botOwnerOnly Whether this command can only be invoked by the owner of the bot.
 * @param serverOwnerOnly Whether this command can only be invoked by the owner of the server.
 * @param privateReply Whether this command's response is sent in a way that only the 
 *                     invoking user can see.
 * @param ephemeralReply The type of ephemeral response to use, if any.
 * @param inheritSettings Whether the command settings should be inherited from the parent 
 *                        command (ignoring the values provided by this command).
 * @param invokeParent Whether to invoke the parent as part of normal execution.
 * @param invocationHandler The handler to use for processing an invocation of the command.
 * @param resultHandlers The handlers to use for processing the result, in order.
 * @version 1.0
 * @since 1.0
 */
public record TextCommandImpl(
        Scope scope,
        boolean callable,
        Invocation parent,
        String name,
        String displayName,
        Set<String> aliases,
        String description,
        List<Parameter<?>> parameters,
        PermissionSet requiredPermissions,
        boolean requireParentPermissions,
        boolean nsfw,
        boolean botOwnerOnly,
        boolean serverOwnerOnly,
        boolean privateReply,
        EphemeralType ephemeralReply,
        boolean inheritSettings,
        boolean invokeParent,
        AnyInvocationHandler invocationHandler,
        List<? extends AnyResultHandler> resultHandlers
) implements TextCommand {

    /**
     * Initializes a new instance.
     *
     * @param scope The scope that the command is defined in.
     * @param callable If the command may be directly invoked by users.
     * @param parent The parent of the command.
     * @param name The name of the command.
     * @param displayName The display name of the command.
     * @param aliases The aliases that may also invoke the command.
     * @param description The description of the command.
     * @param parameters The command parameters, in the order that they should be provided
     *                   by the user.
     * @param requiredPermissions The permissions that a user should have inorder to execute 
     *                            the command.
     * @param requireParentPermissions Whether a user invoking this command must also have 
     *                                 the permissions to invoke its parent command.
     * @param nsfw Whether this command can only be invoked in a NSFW channel.
     * @param botOwnerOnly Whether this command can only be invoked by the owner of the bot.
     * @param serverOwnerOnly Whether this command can only be invoked by the owner of the server.
     * @param privateReply Whether this command's response is sent in a way that only the 
     *                     invoking user can see.
     * @param ephemeralReply The type of ephemeral response to use, if any.
     * @param inheritSettings Whether the command settings should be inherited from the parent 
     *                        command (ignoring the values provided by this command).
     * @param invokeParent Whether to invoke the parent as part of normal execution.
     * @param invocationHandler The handler to use for processing an invocation of the command.
     * @param resultHandlers The handlers to use for processing the result, in order.
     */
    @SideEffectFree
    @SuppressWarnings( "checkstyle:parameternumber" )
    public TextCommandImpl(
            final Scope scope,
            final boolean callable,
            final Invocation parent,
            final String name,
            final String displayName,
            final Set<String> aliases,
            final String description,
            final List<Parameter<?>> parameters,
            final PermissionSet requiredPermissions,
            final boolean requireParentPermissions,
            final boolean nsfw,
            final boolean botOwnerOnly,
            final boolean serverOwnerOnly,
            final boolean privateReply,
            final EphemeralType ephemeralReply,
            final boolean inheritSettings,
            final boolean invokeParent,
            final AnyInvocationHandler invocationHandler,
            final List<? extends AnyResultHandler> resultHandlers
    ) {

        this.scope = Objects.requireNonNull( scope );
        this.callable = callable;
        this.parent = CommandUtils.validateParent( parent );
        this.name = CommandUtils.validateName( name );
        this.displayName = CommandUtils.validateDisplayName( displayName );
        this.aliases = CommandUtils.validateAliases( aliases );
        this.description = CommandUtils.validateDescription( description );
        this.parameters = CommandUtils.validateParameters( parameters );
        this.requiredPermissions = CommandUtils.validatePermissions( requiredPermissions );
        this.requireParentPermissions = requireParentPermissions;
        this.nsfw = nsfw;
        this.botOwnerOnly = botOwnerOnly;
        this.serverOwnerOnly = serverOwnerOnly;
        this.privateReply = privateReply;
        this.ephemeralReply = Objects.requireNonNull( ephemeralReply );
        this.inheritSettings = inheritSettings;
        this.invokeParent = invokeParent;
        this.invocationHandler = CommandUtils.validateInvocationHandler( invocationHandler );
        this.resultHandlers = CommandUtils.validateResultHandlers( resultHandlers );

    }
    
}
