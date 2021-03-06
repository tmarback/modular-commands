package dev.sympho.modular_commands.api.command;

import java.util.Collections;
import java.util.List;

import org.checkerframework.dataflow.qual.Pure;

import dev.sympho.modular_commands.api.command.ReplyManager.EphemeralType;
import dev.sympho.modular_commands.api.command.handler.InvocationHandler;
import dev.sympho.modular_commands.api.command.handler.ResultHandler;
import dev.sympho.modular_commands.api.command.parameter.Parameter;
import dev.sympho.modular_commands.api.permission.AccessValidator;
import dev.sympho.modular_commands.api.permission.Group;

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
     * The scopes that a command may be defined in.
     *
     * @version 1.0
     * @since 1.0
     */
    enum Scope { 

        /**
         * A command that can be invoked in either guild channels or private channels.
         */
        GLOBAL, 

        /**
         * A command that can only be invoked in guild channels.
         */
        GUILD 

    }

    /**
     * The scope that the command is defined in.
     *
     * @return The command scope.
     */
    @Pure
    Scope scope();

    /**
     * Whether this command can be invoked by a user.
     * 
     * <p>A non-callable command is usually used as a parent for other commands that should
     * not be itself invoked.
     * 
     * <p>Do note that slash commands cannot be invoked if they have children commands, and
     * so in that case this parameter has no effect. This is a limitation from Discord itself.
     *
     * @return {@code true} if the command may be directly called by users. 
     *         {@code false} otherwise.
     */
    @Pure
    boolean callable();

    /**
     * The parent of the command.
     * 
     * <p>If the invocation returned by this is empty, this command has
     * no parent, and is therefore a main command. Otherwise, it is a
     * subcommand.
     * 
     * <p>Note that a parent can only be specified by its {@link #name() name}, not
     * any aliases it may have (when supported). This implies that any command that
     * may match the Invocation returned by this method through an alias is <i>not</i>
     * considered a parent of this command.
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
     * The group that a user must have access for in order to invoke this command.
     * 
     * <p>Note that having access to a group is not necessarily the same as belonging
     * to that group (although it is the basic threshold); the specific definition
     * depends on the {@link AccessValidator} implementation used to verify access.
     * 
     * <p>For application commands, be aware that this is completely disjoint from Discord's
     * own permission system, and so utilizing this feature may lead to user confusion due
     * to discrepancies between the apparent configured permissions and the internal group
     * checks. It is nonetheless left as an option for situations where the application
     * should have control over who is allowed to run the command (instead of guild
     * administrators), for example global debug commands that should be reserved for 
     * the developers, and for complex permissions beyond what Discord can support
     * (for example, subcommand-specific access restrictions). Make sure that
     * {@link #skipGroupCheckOnInteraction() group checking is not skipped} when
     * doing this.
     * 
     * <p>To completely skip group checking on invocations made through application
     * commands (possibly saving a few function calls in such cases), use
     * {@link #skipGroupCheckOnInteraction()}. It may also be useful in the case of 
     * commands that support both text and interactions, where group checking may be 
     * wanted only for the text case (where there is no built-in permission system 
     * through Discord).
     *
     * @return The required group.
     */
    @Pure
    Group requiredGroup();

    /**
     * Whether group access checking should be skipped when this command is invoked
     * through an interaction. This option is only meaningful if the command <i>can</i>
     * be invoked through interactions.
     *
     * @return Whether group access checking should be skipped when this command is 
     *         invoked through an interaction.
     * @see #requiredGroup()
     * @implSpec The default returns {@code true}, as application commands should generally
     *           avoid clashing with user-set permissions unless necessary.
     */
    @Pure
    default boolean skipGroupCheckOnInteraction() {
        return true;
    }

    /**
     * Whether a user invoking this command must also have access to the groups
     * necessary to invoke its parent command(s).
     *
     * @return Whether a user invoking this command must also have access to the
     *         groups required by its parents.
     * @see #requiredGroup()
     * @apiNote This value is only meaningful for subcommands.
     */
    @Pure
    boolean requireParentGroups();

    /**
     * Whether this command can only be invoked in a NSFW channel.
     * 
     * <p>A private channel (if the {@link #scope() scope}) of the command allows invocation
     * in one) always satisfies this condition.
     * 
     * <p>Conversely, a guild channel that cannot be marked as NSFW (such as an announcement
     * channel) never satisfies this condition.
     *
     * @return Whether this command can only be invoked in a NSFW channel.
     */
    @Pure
    boolean nsfw();

    /**
     * Whether this command's response is sent in a way that only the invoking user
     * can see.
     *
     * @return Whether this command's response is sent in a way that only the invoking user
     *         can see.
     * @apiNote This setting only affects the default configuration of the reply manager
     *          provided by the execution context. The specific mechanism used for it
     *          depends on how the command was invoked (message, slash command, etc)
     *          and the value of {@link #ephemeralReply()}.
     */
    @Pure
    boolean privateReply();

    /**
     * The type of ephemeral response to use, if any.
     *
     * @return The ephemeral response type.
     * @apiNote This setting only affects the default configuration of the reply manager
     *          provided by the execution context. The specific mechanism used for it
     *          depends on how the command was invoked (message, slash command, etc).
     */
    @Pure
    EphemeralType ephemeralReply();

    /**
     * Whether the command settings should be inherited from the parent command
     * (ignoring the values provided by this command).
     * 
     * <p>The settings are the values provided by the following methods:
     * 
     * <ul>
     *  <li>{@link #nsfw()}</li>
     *  <li>{@link #privateReply()}</li>
     * </ul>
     *
     * @return Whether settings should be inherited from the parent command.
     * @apiNote This value is only meaningful for subcommands.
     */
    @Pure
    boolean inheritSettings();

    /**
     * Whether the parent command should be invoked before this command is invoked.
     * 
     * <p>If the parent command may also define that its parent must be executed, and
     * so on. Execution will start at the closest ancestor that sets this to false or
     * does not have a parent (including itself).
     * 
     * <p>The parent(s) will be invoked with the same context object, which means that a
     * parent may pass objects to a child through the context. It also means that the
     * parameters to this command must be <i>compatible</i> with all executed ancestors,
     * which means that
     * 
     * <ul>
     *  <li>Any parameter that is defined in both this command and an executed parent (that
     *      is, has the same name) must be of the same type.
     *  <li>Any parameter that is <i>required</i> in an executed parent must be defined
     *      in this command, and it must either be also required or have a default value.</li>
     *  <li>The <i>order</i> of shared parameters does <i>not</i> need to be the same or 
     *      similar in any way.</li>
     * </ul>
     * 
     * <p>If the parameters of this command are not compatible with those of any executed
     * parent as defined above, an error will be triggered at execution time.
     * 
     * <p>There are no compatibility requirements for ancestors that would not be executed
     * as part of an execution of this command.
     * 
     * <p>Naturally, this flag has no effect on a command with no parent.
     *
     * @return Whether this command's parent should be executed as part of an invocation.
     */
    @Pure
    boolean invokeParent();

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
    InvocationHandler invocationHandler();

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
