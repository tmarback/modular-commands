package dev.sympho.modular_commands.impl.command;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.Invocation;
import dev.sympho.modular_commands.api.command.MessageCommand;
import dev.sympho.modular_commands.api.command.ReplyManager.EphemeralType;
import dev.sympho.modular_commands.api.command.handler.MessageInvocationHandler;
import dev.sympho.modular_commands.api.command.handler.MessageResultHandler;
import dev.sympho.modular_commands.api.command.parameter.Parameter;
import dev.sympho.modular_commands.api.permission.Group;
import dev.sympho.modular_commands.utils.CommandUtils;

/**
 * Default implementation of a message-based command.
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
 * @param requiredGroup The group that a user must have access for in order to invoke 
 *                      this command.
 * @param requireParentGroups Whether a user invoking this command must also have access 
 *                            to the groups necessary to invoke its parent command(s).
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
public record MessageCommandImpl(
        Scope scope,
        boolean callable,
        Invocation parent,
        String name,
        String displayName,
        Set<String> aliases,
        String description,
        List<Parameter<?>> parameters,
        Group requiredGroup,
        boolean requireParentGroups,
        boolean nsfw,
        boolean botOwnerOnly,
        boolean serverOwnerOnly,
        boolean privateReply,
        EphemeralType ephemeralReply,
        boolean inheritSettings,
        boolean invokeParent,
        MessageInvocationHandler invocationHandler,
        List<? extends MessageResultHandler> resultHandlers
) implements MessageCommand {

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
     * @param requiredGroup The group that a user must have access for in order to invoke 
     *                      this command.
     * @param requireParentGroups Whether a user invoking this command must also have access 
     *                            to the groups necessary to invoke its parent command(s).
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
    public MessageCommandImpl(
            final Scope scope,
            final boolean callable,
            final Invocation parent,
            final String name,
            final String displayName,
            final Set<String> aliases,
            final String description,
            final List<Parameter<?>> parameters,
            final Group requiredGroup,
            final boolean requireParentGroups,
            final boolean nsfw,
            final boolean botOwnerOnly,
            final boolean serverOwnerOnly,
            final boolean privateReply,
            final EphemeralType ephemeralReply,
            final boolean inheritSettings,
            final boolean invokeParent,
            final MessageInvocationHandler invocationHandler,
            final List<? extends MessageResultHandler> resultHandlers
    ) {

        this.scope = Objects.requireNonNull( scope );
        this.callable = callable;
        this.parent = CommandUtils.validateParent( parent );
        this.name = CommandUtils.validateName( name );
        this.displayName = CommandUtils.validateDisplayName( displayName );
        this.aliases = CommandUtils.validateAliases( aliases );
        this.description = CommandUtils.validateDescription( description );
        this.parameters = CommandUtils.validateParameters( parameters );
        this.requiredGroup = CommandUtils.validateGroup( requiredGroup );
        this.requireParentGroups = requireParentGroups;
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
