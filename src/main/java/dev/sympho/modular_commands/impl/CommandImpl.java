package dev.sympho.modular_commands.impl;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.checkerframework.common.value.qual.MatchesRegex;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.Command;
import dev.sympho.modular_commands.api.command.Invocation;
import dev.sympho.modular_commands.api.command.ReplyManager.EphemeralType;
import dev.sympho.modular_commands.api.command.handler.Handlers;
import dev.sympho.modular_commands.api.command.parameter.Parameter;
import dev.sympho.modular_commands.api.permission.Group;
import dev.sympho.modular_commands.utils.CommandUtils;

/**
 * Default implementation of an interaction-based command.
 *
 * @param <H> The handler type.
 * @param id The ID that uniquely identifies this command in the system.
 * @param scope The scope that the command is defined in.
 * @param callable If the command may be directly invoked by users.
 * @param parent The parent of the command.
 * @param name The name of the command.
 * @param aliases The aliases that may also invoke the command.
 * @param displayName The display name of the command.
 * @param description The description of the command.
 * @param parameters The command parameters, in the order that they should be provided
 *                   by the user.
 * @param requiredGroup The group that a user must have access for in order to invoke 
 *                      this command.
 * @param skipGroupCheckOnInteraction Whether group access checking should be skipped.
 * @param requireParentGroups Whether a user invoking this command must also have access 
 *                            to the groups necessary to invoke its parent command(s).
 * @param nsfw Whether this command can only be invoked in a NSFW channel.
 * @param privateReply Whether this command's response is sent in a way that only the 
 *                     invoking user can see.
 * @param ephemeralReply The type of ephemeral response to use, if any.
 * @param inheritSettings Whether the command settings should be inherited from the parent 
 *                        command (ignoring the values provided by this command).
 * @param invokeParent Whether to invoke the parent as part of normal execution.
 * @param handlers The handler to use for processing an invocation of the command.
 * @version 1.0
 * @since 1.0
 */
public record CommandImpl<H extends Handlers>(
        String id,
        Scope scope,
        boolean callable,
        Invocation parent,
        @MatchesRegex( Command.NAME_REGEX ) String name,
        Set<@MatchesRegex( Command.NAME_REGEX ) String> aliases,
        @MatchesRegex( Command.DISPLAY_NAME_REGEX ) String displayName,
        @MatchesRegex( Command.DESCRIPTION_REGEX ) String description,
        List<Parameter<?>> parameters,
        Group requiredGroup,
        boolean skipGroupCheckOnInteraction,
        boolean requireParentGroups,
        boolean nsfw,
        boolean privateReply,
        EphemeralType ephemeralReply,
        boolean inheritSettings,
        boolean invokeParent,
        H handlers
) implements Command<H> {

    /**
     * Initializes a new instance.
     *
     * @param id The ID that uniquely identifies this command in the system.
     * @param scope The scope that the command is defined in.
     * @param callable If the command may be directly invoked by users.
     * @param parent The parent of the command.
     * @param name The name of the command.
     * @param aliases The aliases that may also invoke the command.
     * @param displayName The display name of the command.
     * @param description The description of the command.
     * @param parameters The command parameters, in the order that they should be provided
     *                   by the user.
     * @param requiredGroup The group that a user must have access for in order to invoke 
     *                      this command.
     * @param skipGroupCheckOnInteraction Whether group access checking should be skipped.
     * @param requireParentGroups Whether a user invoking this command must also have access 
     *                            to the groups necessary to invoke its parent command(s).
     * @param nsfw Whether this command can only be invoked in a NSFW channel.
     * @param privateReply Whether this command's response is sent in a way that only the 
     *                     invoking user can see.
     * @param ephemeralReply The type of ephemeral response to use, if any.
     * @param inheritSettings Whether the command settings should be inherited from the parent 
     *                        command (ignoring the values provided by this command).
     * @param invokeParent Whether to invoke the parent as part of normal execution.
     * @param handlers The handler to use for processing an invocation of the command.
     */
    @SideEffectFree
    @SuppressWarnings( "checkstyle:parameternumber" )
    public CommandImpl(
            final String id,
            final Scope scope,
            final boolean callable,
            final Invocation parent,
            final @MatchesRegex( Command.NAME_REGEX ) String name,
            final Set<@MatchesRegex( Command.NAME_REGEX ) String> aliases,
            final @MatchesRegex( Command.DISPLAY_NAME_REGEX ) String displayName,
            final @MatchesRegex( Command.DESCRIPTION_REGEX ) String description,
            final List<Parameter<?>> parameters,
            final Group requiredGroup,
            final boolean skipGroupCheckOnInteraction,
            final boolean requireParentGroups,
            final boolean nsfw,
            final boolean privateReply,
            final EphemeralType ephemeralReply,
            final boolean inheritSettings,
            final boolean invokeParent,
            final H handlers
    ) {

        this.id = CommandUtils.validateId( id );
        this.scope = Objects.requireNonNull( scope );
        this.callable = callable;
        this.parent = CommandUtils.validateParent( parent );
        this.name = CommandUtils.validateName( name );
        this.aliases = CommandUtils.validateAliases( aliases );
        this.displayName = CommandUtils.validateDisplayName( displayName );
        this.description = CommandUtils.validateDescription( description );
        this.parameters = CommandUtils.validateParameters( parameters );
        this.requiredGroup = CommandUtils.validateGroup( requiredGroup );
        this.skipGroupCheckOnInteraction = skipGroupCheckOnInteraction;
        this.requireParentGroups = requireParentGroups;
        this.nsfw = nsfw;
        this.privateReply = privateReply;
        this.ephemeralReply = Objects.requireNonNull( ephemeralReply );
        this.inheritSettings = inheritSettings;
        this.invokeParent = invokeParent;
        this.handlers = CommandUtils.validateHandlers( handlers );

    }
    
}
