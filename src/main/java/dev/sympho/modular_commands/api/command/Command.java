package dev.sympho.modular_commands.api.command;

import java.util.Collections;
import java.util.List;

import org.checkerframework.dataflow.qual.Pure;

import dev.sympho.modular_commands.api.command.handler.CommandHandler;
import dev.sympho.modular_commands.api.command.handler.ResultHandler;
import dev.sympho.modular_commands.api.command.parameter.Parameter;
import discord4j.rest.util.PermissionSet;

// BEGIN LONG LINES
/**
 * A command that can be invoked by a user.
 * 
 * <p>Irrespective of whether the command it is used with is compatible with interactions
 * or not, all values must be compatible with the
 * <a href="https://discord.com/developers/docs/interactions/application-commands#application-command-object">
 * Discord API specification</a> for command parameters.
 *
 * @version 1.0
 * @since 1.0
 */
// END LONG LINES
public sealed interface Command 
        permits MessageCommand, SlashCommand {

    /**
     * The parent of the command.
     * 
     * <p>If the invocation returned by this is empty, this command has
     * no parent, and is therefore a main command. Otherwise, it is a
     * subcommand.
     *
     * @return The parent of the command.
     */
    @Pure
    Invocation parent();

    /**
     * The name of the command.
     * 
     * <p>This is the name used to match the command internally and for accessing
     * text-based commands (slash and text commands). It is provided to the Discord
     * API for slash commands, but <i>not</i> for user and message commands (for
     * that, see {@link #displayName()}).
     *
     * @return The name of the command.
     */
    @Pure
    String name();

    /**
     * The invocation that executes this command.
     *
     * @return The command invocation.
     * @implSpec The invocation is determined by appending the {@link #name() name}
     *           to the {@link #parent() parent}.
     */
    @Pure
    default Invocation invocation() {

        return parent().child( name() );

    }

    /**
     * The display name of the command.
     * 
     * <p>This is displayed to the user in documentation-related functionality.
     * For user and message commands, this is also the value provided to the
     * Discord API as the command name instead of {@link #name()}.
     *
     * @return The display name of the command.
     */
    @Pure
    String displayName();

    /**
     * The description of the command.
     *
     * @return The description of the command.
     */
    @Pure
    String description();

    /**
     * The command parameters, in the order that they should be provided
     * by the user.
     * 
     * <p>All parameters that are marked as {@link Parameter#required() required}
     * <i>must</i> come <i>before</i> all parameters that aren't (optional parameters).
     *
     * @return The command parameters.
     * @apiNote The restriction in parameter order is due to the fact that it would
     *          not make any sense to have an optional parameter before a required one.
     *          Since all parameters must be provided in sequence, that would effectively
     *          make the optional parameter required.
     */
    @Pure
    List<Parameter<?>> parameters();



    /**
     * The built-in permissions that a user should have in order to execute the
     * command.
     *
     * @return The built-in permissions required to run the command.
     */
    @Pure
    PermissionSet requiredDiscordPermissions();

    /**
     * Whether a user invoking this command must also have the permissions
     * to invoke its parent command.
     *
     * @return Whether a user invoking this command must also have the permissions
     *         to invoke the parent command.
     * @apiNote This value is only meaningful for subcommands.
     */
    @Pure
    boolean requireParentPermissions();

    /**
     * Whether this command can only be invoked in a NSFW channel.
     *
     * @return Whether this command can only be invoked in a NSFW channel.
     */
    @Pure
    boolean nsfw();

    /**
     * Whether this command can only be invoked by the owner of the bot.
     *
     * @return Whether this command can only be invoked by the owner of the bot.
     */
    @Pure
    boolean botOwnerOnly();

    /**
     * Whether this command can only be invoked by the owner of the server.
     *
     * @return Whether this command can only be invoked by the owner of the server.
     */
    @Pure
    boolean serverOwnerOnly();

    /**
     * Whether this command's response is sent in a way that only the invoking user
     * can see.
     *
     * @return Whether this command's response is sent in a way that only the invoking user
     *         can see.
     * @apiNote This setting only affects the default configuration of the reply manager
     *          provided by the execution context. The specific mechanism used for it
     *          depends on how the command was invoked (message, slash command, etc).
     */
    @Pure
    boolean privateReply();


    /**
     * Whether the command settings should be inherited from the parent command
     * (ignoring the values provided by this command).
     * 
     * <p>The settings are the values provided by the following methods:
     * 
     * <ul>
     *  <li>{@link #nsfw()}</li>
     *  <li>{@link #botOwnerOnly()}</li>
     *  <li>{@link #serverOwnerOnly()}</li>
     *  <li>{@link #privateReply()}</li>
     * </ul>
     *
     * @return Whether settings should be inherited from the parent command.
     * @apiNote This value is only meaningful for subcommands.
     */
    @Pure
    boolean inheritSettings();

    /**
     * The handler to use for processing an invocation of the command.
     *
     * @return The handler.
     * @apiNote Why require the use of a handler object instead of using a plain method, you ask?
     *          Because the Java compiler is dumb and doesn't automatically deal with widening
     *          parameter type overloads, which leads to a lot of boilerplate to get this handler
     *          structure working, and I have to make the separate handler classes anyway, so I'd
     *          rather not duplicate all of that.
     *          <br>Making Command extend CommandHandler would also be a solution, except that
     *          sealed classes don't work across packages without using modules. So yeah.
     */
    @Pure
    CommandHandler invocationHandler();

    /**
     * The handlers to use for processing the result, in order.
     * 
     * <p>They are given priority over any handlers defined at the registry
     * or global level.
     *
     * @return The result handlers for this command.
     * @implSpec The default is to have no handlers.
     */
    @Pure
    default List<? extends ResultHandler> resultHandlers() {
        return Collections.emptyList();
    }
    
}
