package dev.sympho.modular_commands.api.command;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.regex.qual.Regex;
import org.checkerframework.common.value.qual.MatchesRegex;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;
import org.immutables.value.Value;

import dev.sympho.bot_utils.access.AccessManager;
import dev.sympho.bot_utils.access.Group;
import dev.sympho.bot_utils.access.Groups;
import dev.sympho.bot_utils.event.reply.ReplyManager;
import dev.sympho.bot_utils.event.reply.ReplySpec;
import dev.sympho.modular_commands.api.command.handler.Handlers;
import dev.sympho.modular_commands.api.command.handler.InteractionHandlers;
import dev.sympho.modular_commands.api.command.handler.MessageHandlers;
import dev.sympho.modular_commands.api.command.handler.SlashHandlers;
import dev.sympho.modular_commands.api.command.handler.TextHandlers;
import dev.sympho.modular_commands.api.command.parameter.Parameter;
import dev.sympho.modular_commands.utils.CommandUtils;

// BEGIN LONG LINES
/**
 * A command that can be invoked by a user.
 * 
 * <p>Irrespective of whether the command it is used with is compatible with interactions
 * or not, all values must be compatible with the
 * <a href="https://discord.com/developers/docs/interactions/application-commands#application-command-object">
 * Discord API specification</a> for command parameters.
 *
 * @param <H> The handler type of this command. The subtype(s) of {@link Handlers} used determine
 *            what methods of invocation are supported by this command. However, this type
 *            parameter is used only to facilitate type checking, with actual support being
 *            determined by the concrete type of the return value of {@link #handlers()}. See
 *            the description of that method for details.
 * @version 1.0
 * @since 1.0
 */
// END LONG LINES
@Value.Immutable
@Value.Style( 
        visibility = Value.Style.ImplementationVisibility.PACKAGE, 
        overshadowImplementation = true 
)
public interface Command<H extends @NonNull Handlers> {

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

    /* Defaults */

    /** Default for {@link #scope()} ({@link Scope#GLOBAL}). */
    Scope DEFAULT_SCOPE = Scope.GLOBAL;

    /** Default for {@link #callable()}. */
    boolean DEFAULT_CALLABLE = true;

    /** Default for {@link #requiredGroup()} ({@link Groups#EVERYONE}). */
    Group DEFAULT_GROUP = Groups.EVERYONE;

    /** Default for {@link #skipGroupCheckOnInteraction()}. */
    boolean DEFAULT_SKIP = true;

    /** Default for {@link #requireParentGroups()}. */
    boolean DEFAULT_REQUIRE_PARENT_GROUPS = true;

    /** Default for {@link #nsfw()}. */
    boolean DEFAULT_NSFW = false;

    /** Default for {@link #repliesDefaultPrivate()}. */
    boolean DEFAULT_PRIVATE = false;

    /** Default for {@link #deferReply()}. */
    boolean DEFAULT_DEFER = false;

    /** Default for {@link #inheritSettings()}. */
    boolean DEFAULT_INHERIT = false;

    /** Default for {@link #invokeParent()}. */
    boolean DEFAULT_INVOKE_PARENT = false;

    /* String regexes */

    /** Pattern for valid slash command names in the Discord API. */
    @Regex String NAME_REGEX = "(?U)^[-_\\p{L}\\p{N}\\p{sc=Deva}\\p{sc=Thai}]{1,32}+$";
    /** Pattern for valid user/message command names in the Discord API. */
    @Regex String DISPLAY_NAME_REGEX = "(?U)^[ -_\\p{L}\\p{N}\\p{sc=Deva}\\p{sc=Thai}]{1,32}+$";
    /** Pattern for valid command descriptions in the Discord API. */
    @Regex String DESCRIPTION_REGEX = "(?Us)^.{1,100}+$";

    /**
     * The ID that uniquely identifies this command in the system.
     *
     * @return The command ID.
     */
    @Pure
    String id();

    /**
     * The scope that the command is defined in.
     *
     * @return The command scope.
     * @implSpec The default value is {@link #DEFAULT_SCOPE}.
     */
    @Pure
    @Value.Default
    default Scope scope() {
        return Scope.GLOBAL;
    }

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
     * @implSpec The default value is {@value #DEFAULT_CALLABLE}.
     */
    @Pure
    @Value.Default
    default boolean callable() {
        return DEFAULT_CALLABLE;
    }

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
     * @implSpec The default is {@link Invocation#EMPTY the empty invocation} (e.g. no parent).
     */
    @Pure
    @Value.Default
    default Invocation parent() {
        return Invocation.EMPTY;
    }

    /**
     * The name of the command.
     * 
     * <p>The name must be compatible with the Discord API, thus it must match the
     * regex {@value #NAME_REGEX} and all letters used must be their lowercase variant,
     * if there is one.
     * 
     * <p>This is the name used to match the command internally and for accessing
     * text-based commands (slash and text commands). It is provided to the Discord
     * API for slash commands, but <i>not</i> for user and message commands (for
     * that, see {@link #displayName()}).
     *
     * @return The name of the command.
     */
    @Pure
    @MatchesRegex( NAME_REGEX ) String name();

    /**
     * The invocation that executes this command.
     *
     * @return The command invocation.
     * @implSpec The invocation is determined by appending the {@link #name() name}
     *           to the {@link #parent() parent}.
     */
    @SideEffectFree
    @Value.NonAttribute
    default Invocation invocation() {

        return parent().child( name() );

    }

    /**
     * The aliases that may also invoke the command, when invoking through a message.
     * 
     * <p>These aliases are only relevant to handling invocations. They may not be
     * used to specify the command as a parent.
     * 
     * <p>Aliases must satisfy the same restrictions as the {@link #name() name}.
     *
     * @return The command aliases.
     * @apiNote This setting is only meaningful for message-based invocations. By extension,
     *          if the command does not support message invocations, this setting has no
     *          effect.
     * @implSpec The default is an empty set.
     */
    @Pure
    @Value.Default
    default Set<@MatchesRegex( NAME_REGEX ) String> aliases() {
        return Collections.emptySet();
    }

    /**
     * The alias invocations that may also invoke the command.
     *
     * @return The command aliases as invocations.
     * @see #aliases()
     * @apiNote This value is only meaningful for message-based invocations. By extension,
     *          if the command does not support message invocations, this setting has no
     *          effect.
     * @implSpec The invocation is determined by appending each alias to the 
     *           {@link #parent() parent}. Do not override this.
     */
    @SideEffectFree
    @Value.NonAttribute
    default Set<Invocation> aliasInvocations() {

        return aliases().stream()
                .map( this.parent()::child )
                .collect( Collectors.toUnmodifiableSet() );

    }

    /**
     * The display name of the command.
     * 
     * <p>The name must be compatible with the Discord API, thus it must match the
     * regex {@value #DISPLAY_NAME_REGEX}.
     * 
     * <p>This is displayed to the user in documentation-related functionality.
     * For user and message commands, this is also the value provided to the
     * Discord API as the command name instead of {@link #name()}.
     *
     * @return The display name of the command.
     */
    @Pure
    @MatchesRegex( DISPLAY_NAME_REGEX ) String displayName();

    /**
     * The description of the command.
     *
     * @return The description of the command.
     */
    @Pure
    @MatchesRegex( DESCRIPTION_REGEX ) String description();

    /**
     * The command parameters, in the order that they should be provided by the user.
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
     * depends on the {@link AccessManager} implementation used to manage access.
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
     * @implSpec The default value is {@link #DEFAULT_GROUP}.
     */
    @Pure
    @Value.Default
    default Group requiredGroup() {
        return DEFAULT_GROUP;
    }

    /**
     * Whether group access checking should be skipped when this command is invoked
     * through an interaction.
     *
     * @return Whether group access checking should be skipped when this command is 
     *         invoked through an interaction.
     * @see #requiredGroup()
     * @apiNote This value is only meaningful for interaction-based invocations. By extension,
     *          if the command does not support interaction invocations, this setting has no
     *          effect.
     * @implSpec The default value is {@value #DEFAULT_SKIP}, as application commands 
     *           should generally avoid clashing with user-set permissions unless necessary.
     */
    @Pure
    @Value.Default
    default boolean skipGroupCheckOnInteraction() {
        return DEFAULT_SKIP;
    }

    /**
     * Whether a user invoking this command must also have access to the groups
     * necessary to invoke its parent command(s).
     *
     * @return Whether a user invoking this command must also have access to the
     *         groups required by its parents.
     * @see #requiredGroup()
     * @apiNote This value is only meaningful for subcommands.
     * @implSpec The default value is {@value #DEFAULT_REQUIRE_PARENT_GROUPS}.
     */
    @Pure
    @Value.Default
    default boolean requireParentGroups() {
        return DEFAULT_REQUIRE_PARENT_GROUPS;
    }

    /**
     * Whether this command can only be invoked in a NSFW channel.
     * 
     * <p>A private channel (if the {@link #scope() scope} of the command allows invocation
     * in one) always satisfies this condition.
     * 
     * <p>Conversely, a guild channel that cannot be marked as NSFW (such as an announcement
     * channel) never satisfies this condition.
     *
     * @return Whether this command can only be invoked in a NSFW channel.
     * @implSpec The default value is {@value #DEFAULT_NSFW}.
     */
    @Pure
    @Value.Default
    default boolean nsfw() {
        return DEFAULT_NSFW;
    }

    /**
     * Whether the replies sent by this command should be private by default (i.e. unless 
     * specified otherwise on the individual reply).
     *
     * @return Whether this command's responses should be private by default.
     * @see ReplySpec#privately()
     * @apiNote The specific mechanism used for private replies may vary depending on how 
     *          the command was invoked (message, slash command, etc).
     * @implSpec The default value is {@value #DEFAULT_PRIVATE}.
     */
    @Pure
    @Value.Default
    default boolean repliesDefaultPrivate() {
        return DEFAULT_PRIVATE;
    }

    /**
     * Whether the initial reply to the command should be automatically deferred.
     * 
     * <p>Note that whether the deferral is publicly visible or not is defined by the
     * {@link #repliesDefaultPrivate() default visibility of replies}. Note also that,
     * if deferral is used, the visibility of the first reply will be set to the default
     * to match the deferral, with the value of {@link ReplySpec#privately()} 
     * being ignored if set. Further replies are not affected.
     * 
     * <p>It is also possible to manually defer the response at handling time using
     * {@link ReplyManager#defer()}, with the same effect (in fact, the handler will
     * internally call that method if this is {@code true}). The advantage of setting
     * this to {@code true} is, beyond avoiding the need for an extra function call
     * and map, that the handler will defer <i>before</i> performing any access checks
     * or argument parsing, which may be useful if any of those stages takes a long time.
     *
     * @return Whether this command's initial response is deferred.
     * @apiNote The specific mechanism used for deferral may vary depending on how 
     *          the command was invoked (message, slash command, etc).
     * @implSpec The default value is {@value #DEFAULT_DEFER}.
     */
    @Pure
    @Value.Default
    default boolean deferReply() {
        return DEFAULT_DEFER;
    }

    /**
     * Whether the command settings should be inherited from the parent command
     * (ignoring the values provided by this command).
     * 
     * <p>The settings are the values provided by the following methods:
     * 
     * <ul>
     *  <li>{@link #nsfw()}</li>
     *  <li>{@link #repliesDefaultPrivate()}</li>
     * </ul>
     *
     * @return Whether settings should be inherited from the parent command.
     * @apiNote This value is only meaningful for subcommands.
     * @implSpec The default value is {@value #DEFAULT_INHERIT}.
     */
    @Pure
    @Value.Default
    default boolean inheritSettings() {
        return DEFAULT_INHERIT;
    }

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
     * @implSpec The default value is {@value #DEFAULT_INVOKE_PARENT}.
     */
    @Pure
    @Value.Default
    default boolean invokeParent() {
        return DEFAULT_INVOKE_PARENT;
    }

    /**
     * The handlers to use for processing an invocation of the command.
     * 
     * <p>Note that the subtype(s) of {@link Handlers} implemented by the returned instance 
     * determine what methods of invocation are supported by this command. For example, if 
     * it implements only {@link MessageHandlers}, it will only support message commands;
     * if it implements both {@link MessageHandlers} and {@link SlashHandlers}, it will
     * support both message commands and slash commands; and so on.
     * 
     * <p>Be careful that, while this interface does have a generic type parameter to constrain 
     * the allowed return type for this method, it is used only to facilitate type checking
     * (especially internally). The invocation support is determined by the actual type of
     * the instance returned by this method. That is, if this method returns an object that
     * implements both {@link MessageHandlers} and {@link SlashHandlers}, this command will
     * support both message and slash commands, even if this instance is created as of type 
     * {@code Command<MessageHandlers>}. There is no way around this due to the nature of
     * type erasure.
     *
     * @return The handler.
     */
    @Pure
    H handlers();

    /**
     * Validates that the command properties of this instance are valid.
     *
     * @throws IllegalArgumentException if any of properties are invalid.
     * @throws NullPointerException if a {@code null} value was found where not allowed.
     * @see CommandUtils#validateCommand(Command)
     */
    @Pure
    @Value.Check
    default void validate() throws IllegalArgumentException, NullPointerException {

        CommandUtils.validateCommand( this );

    }

    /**
     * Creates a new builder.
     *
     * @param <H> The handler type.
     * @return The builder.
     */
    @SideEffectFree
    static <H extends @NonNull Handlers> Builder<H> builder() {
        return new Builder<>();
    }

    /**
     * Creates a new builder initialized with the properties of the given command.
     *
     * @param <H> The argument type.
     * @param base The base instance to copy.
     * @return The builder.
     */
    @SideEffectFree
    static <H extends @NonNull Handlers> Builder<H> builder( final Command<H> base ) {
        return new Builder<H>().from( base );
    }

    /**
     * Creates a builder for a message-only command.
     *
     * @return The builder.
     * @apiNote Use to force the type argument when it cannot be inferred.
     */
    @SideEffectFree
    static Builder<MessageHandlers> message() {
        return builder();
    }

    /**
     * Creates a builder for a slash-only command.
     *
     * @return The builder.
     * @apiNote Use to force the type argument when it cannot be inferred.
     */
    @SideEffectFree
    static Builder<SlashHandlers> slash() {
        return builder();
    }

    /**
     * Creates a builder for a text (message and slash) command.
     *
     * @return The builder.
     * @apiNote Use to force the type argument when it cannot be inferred.
     */
    @SideEffectFree
    static Builder<TextHandlers> text() {
        return builder();
    }

    /**
     * Creates a builder for an interaction command.
     *
     * @return The builder.
     * @apiNote Use to force the type argument when it cannot be inferred.
     */
    @SideEffectFree
    static Builder<InteractionHandlers> interaction() {
        return builder();
    }

    /**
     * The default builder.
     *
     * @param <H> The handler type.
     * @since 1.0
     */
    @SuppressWarnings( "MissingCtor" )
    class Builder<H extends @NonNull Handlers> extends ImmutableCommand.Builder<H> {

        /**
         * Initializes the value for the {@link Command#parent() parent} attribute.
         * 
         * <p><em>If not set, this attribute will have a default value as returned by 
         * the initializer of {@link Command#parent() parent}.</em>
         *
         * @param parent The parent command.
         * @return {@code this} builder for use in a chained invocation.
         * @see #parent(Invocation)
         * @see Command#invocation()
         * @apiNote This is a shortcut to use an existing command instance instead of having
         *          to separately maintain the parent invocation. However, note that creating
         *          entirely new instances for this may have performance implications; it is
         *          best used with an existing instance (such as the one to be registered)
         *          when possible.
         */
        public final Command.Builder<H> parent( final Command<?> parent ) {

            return parent( Objects.requireNonNull( parent, "parent" ).invocation() );
            
        }

    }
    
}
