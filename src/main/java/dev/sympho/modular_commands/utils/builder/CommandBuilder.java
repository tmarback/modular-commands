package dev.sympho.modular_commands.utils.builder;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.common.returnsreceiver.qual.This;
import org.checkerframework.dataflow.qual.Deterministic;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.Command;
import dev.sympho.modular_commands.api.command.Command.Scope;
import dev.sympho.modular_commands.api.command.Invocation;
import dev.sympho.modular_commands.api.command.ReplyManager.EphemeralType;
import dev.sympho.modular_commands.api.command.handler.Handlers;
import dev.sympho.modular_commands.api.command.handler.InteractionHandlers;
import dev.sympho.modular_commands.api.command.handler.MessageHandlers;
import dev.sympho.modular_commands.api.command.handler.SlashHandlers;
import dev.sympho.modular_commands.api.command.handler.TextHandlers;
import dev.sympho.modular_commands.api.command.parameter.Parameter;
import dev.sympho.modular_commands.api.permission.Group;
import dev.sympho.modular_commands.api.permission.Groups;
import dev.sympho.modular_commands.impl.CommandImpl;
import dev.sympho.modular_commands.utils.CommandUtils;

/**
 * Builder for commands.
 *
 * @param <H> The handler type.
 * @see Command
 * @version 1.0
 * @since 1.0
 */
@SuppressWarnings( "checkstyle:hiddenfield" )
public class CommandBuilder<H extends Handlers> {

    /* Defaults */

    /** Default for {@link #withScope(Scope)} ({@link Scope#GLOBAL}). */
    public static final Scope DEFAULT_SCOPE = Scope.GLOBAL;

    /** Default for {@link #withCallable(boolean)}. */
    public static final boolean DEFAULT_CALLABLE = true;

    /** Default for {@link #requireGroup(Group)} ({@link Groups#EVERYONE}). */
    public static final Group DEFAULT_GROUP = Groups.EVERYONE;

    /** Default for {@link #setSkipGroupCheckOnInteraction(boolean)}. */
    public static final boolean DEFAULT_SKIP = true;

    /** Default for {@link #setRequireParentGroups(boolean)}. */
    public static final boolean DEFAULT_REQUIRE_PARENT_GROUPS = true;

    /** Default for {@link #setNsfw(boolean)}. */
    public static final boolean DEFAULT_NSFW = false;

    /** Default for {@link #setPrivateReply(boolean)}. */
    public static final boolean DEFAULT_PRIVATE = false;

    /** Default for {@link #setEphemeralReply(EphemeralType)} ({@link EphemeralType#NONE}). */
    public static final EphemeralType DEFAULT_EPHEMERAL = EphemeralType.NONE;

    /** Default for {@link #setInheritSettings(boolean)}. */
    public static final boolean DEFAULT_INHERIT = false;

    /** Default for {@link #setInvokeParent(boolean)}. */
    public static final boolean DEFAULT_INVOKE_PARENT = false;

    /* Configuration */

    /** The command scope. */
    protected Scope scope;

    /** Whether the command can be invoked directly. */
    protected boolean callable;

    /** The command parent. */
    protected Invocation parent;

    /** The command name. */
    protected @MonotonicNonNull String name;

    /** The command aliases. */
    protected Set<String> aliases;

    /** The display name. */
    protected @MonotonicNonNull String displayName;

    /** The command description. */
    protected @MonotonicNonNull String description;

    /** The command parameters. */
    protected List<Parameter<?>> parameters;

    /** The group that a user must have access for. */
    protected Group requiredGroup;

    /** Whether group access checking should be skipped. */
    protected boolean skipGroupCheckOnInteraction;

    /** Whether to also require the parent command's groups. */
    protected boolean requireParentGroups;

    /** Whether the command can only be run in NSFW channels. */
    protected boolean nsfw;

    /** Whether the command response should only be seen by the caller. */
    protected boolean privateReply;

    /** The type of ephemeral response to use, if any. */
    protected EphemeralType ephemeralReply;

    /** Whether to inherit settings from parent. */
    protected boolean inheritSettings;

    /** Whether to execute the parent. */
    protected boolean invokeParent;

    /** The handlers to process invocations with. */
    protected @MonotonicNonNull H handlers;

    /**
     * Constructs a new builder with default values.
     */
    @SideEffectFree
    public CommandBuilder() {

        this.scope = DEFAULT_SCOPE;
        this.callable = DEFAULT_CALLABLE;
        this.parent = Invocation.of();
        this.name = null;
        this.aliases = new HashSet<>();
        this.displayName = null;
        this.parameters = new LinkedList<>();
        this.requiredGroup = DEFAULT_GROUP;
        this.skipGroupCheckOnInteraction = DEFAULT_SKIP;
        this.requireParentGroups = DEFAULT_REQUIRE_PARENT_GROUPS;
        this.nsfw = DEFAULT_NSFW;
        this.privateReply = DEFAULT_PRIVATE;
        this.ephemeralReply = DEFAULT_EPHEMERAL;
        this.inheritSettings = DEFAULT_INHERIT;
        this.invokeParent = DEFAULT_INVOKE_PARENT;
        this.handlers = null;

    }

    /**
     * Constructs a new builder that is a copy of the given builder.
     *
     * @param base The builder to copy.
     */
    @SideEffectFree
    public CommandBuilder( final CommandBuilder<? extends H> base ) {

        this.scope = base.scope;
        this.callable = base.callable;
        this.parent = base.parent;
        this.name = base.name;
        this.aliases = new HashSet<>( base.aliases );
        this.displayName = base.displayName;
        this.parameters = new LinkedList<>( base.parameters );
        this.requiredGroup = base.requiredGroup;
        this.skipGroupCheckOnInteraction = base.skipGroupCheckOnInteraction;
        this.requireParentGroups = base.requireParentGroups;
        this.nsfw = base.nsfw;
        this.privateReply = base.privateReply;
        this.ephemeralReply = base.ephemeralReply;
        this.inheritSettings = base.inheritSettings;
        this.invokeParent = base.invokeParent;
        this.handlers = base.handlers;

    }

    /**
     * Constructs a new builder that is initialized to make a copy of 
     * the given command.
     *
     * @param base The command to copy.
     * @throws IllegalArgumentException if the given command has invalid values.
     */
    @SideEffectFree
    public CommandBuilder( final Command<? extends H> base ) throws IllegalArgumentException {

        CommandUtils.validateCommand( base );

        this.scope = base.scope();
        this.callable = base.callable();
        this.parent = base.parent();
        this.name = base.name();
        this.aliases = new HashSet<>( base.aliases() );
        this.displayName = base.displayName();
        this.parameters = new LinkedList<>( base.parameters() );
        this.requiredGroup = base.requiredGroup();
        this.skipGroupCheckOnInteraction = base.skipGroupCheckOnInteraction();
        this.requireParentGroups = base.requireParentGroups();
        this.nsfw = base.nsfw();
        this.privateReply = base.privateReply();
        this.ephemeralReply = base.ephemeralReply();
        this.inheritSettings = base.inheritSettings();
        this.invokeParent = base.invokeParent();
        this.handlers = base.handlers();

    }

    /* Typed factories */

    /**
     * Creates a builder for a message-only command.
     *
     * @return The builder.
     * @apiNote Use to force the type argument when it cannot be inferred.
     */
    public static CommandBuilder<MessageHandlers> message() {
        return new CommandBuilder<>();
    }

    /**
     * Creates a builder for a slash-only command.
     *
     * @return The builder.
     * @apiNote Use to force the type argument when it cannot be inferred.
     */
    public static CommandBuilder<SlashHandlers> slash() {
        return new CommandBuilder<>();
    }

    /**
     * Creates a builder for a text (message and slash) command.
     *
     * @return The builder.
     * @apiNote Use to force the type argument when it cannot be inferred.
     */
    public static CommandBuilder<TextHandlers> text() {
        return new CommandBuilder<>();
    }

    /**
     * Creates a builder for an interaction command.
     *
     * @return The builder.
     * @apiNote Use to force the type argument when it cannot be inferred.
     */
    public static CommandBuilder<InteractionHandlers> interaction() {
        return new CommandBuilder<>();
    }

    /* Building */

    /**
     * Sets the command scope.
     * 
     * <p>The default value is {@link #DEFAULT_SCOPE}.
     *
     * @param scope The command scope. If {@code null}, restores the default value.
     * @return This builder.
     * @see Command#scope()
     */
    @Deterministic
    public @This CommandBuilder<H> withScope( final @Nullable Scope scope ) {

        this.scope = Objects.requireNonNullElse( scope, DEFAULT_SCOPE );
        return this;

    }

    /**
     * Sets whether the command can be invoked directly.
     * 
     * <p>The default value is {@value #DEFAULT_CALLABLE}.
     *
     * @param callable Whether a user can invoke the command directly.
     * @return This builder.
     * @see Command#callable()
     */
    @Deterministic
    public @This CommandBuilder<H> withCallable( final boolean callable ) {

        this.callable = callable;
        return this;

    }

    /**
     * Sets the parent command.
     * 
     * <p>The default value is an empty invocation (no parent).
     *
     * @param parent The invocation of the parent command. If {@code null}, restores
     *               the default value.
     * @return This builder.
     * @see Command#parent()
     */
    @Deterministic
    public @This CommandBuilder<H> withParent( final @Nullable Invocation parent ) {

        this.parent = CommandUtils.validateParent( 
                Objects.requireNonNullElse( parent, Invocation.of() ) );
        return this;

    }

    /**
     * Sets the name of the command.
     * 
     * <p>The default value is {@code null}, e.g. no value. It <i>must</i> be
     * specified before calling {@link #build()} (so this method must be called
     * at least once).
     *
     * @param name The command name.
     * @return This builder.
     * @see Command#name()
     */
    @Deterministic
    public @This CommandBuilder<H> withName( final String name ) {

        this.name = CommandUtils.validateName( name );
        return this;

    }

    /**
     * Sets the aliases that a user should support.
     * 
     * <p>The default value is an empty set (no aliases).
     *
     * @param aliases The command aliases. If {@code null}, restores the default
     *                value.
     * @return This builder.
     * @throws IllegalArgumentException if one of the aliases is not valid.
     * @see Command#aliases()
     */
    @Deterministic
    public @This CommandBuilder<H> withAliases( final @Nullable Set<String> aliases ) 
            throws IllegalArgumentException {

        this.aliases = new HashSet<>( CommandUtils.validateAliases(
                Objects.requireNonNullElse( aliases, Collections.emptySet() ) ) );
        return this;

    }

    /**
     * Adds an alias to the command.
     *
     * @param alias The alias to add.
     * @return This builder.
     * @throws IllegalArgumentException if the alias is not valid.
     * @see Command#aliases()
     */
    @Deterministic
    public @This CommandBuilder<H> addAliases( final String alias ) 
            throws IllegalArgumentException {

        this.aliases.add( CommandUtils.validateAlias( alias ) );
        return this;

    }

    /**
     * Removes all aliases from the command.
     *
     * @return This builder.
     * @see Command#aliases()
     */
    @Deterministic
    public @This CommandBuilder<H> noAliases() {

        this.aliases.clear();
        return this;

    }

    /**
     * Sets the display name of the command.
     * 
     * <p>The default value is {@code null}, e.g. no value. It <i>must</i> be
     * specified before calling {@link #build()} (so this method must be called
     * at least once).
     *
     * @param name The command display name.
     * @return This builder.
     * @see Command#displayName()
     */
    @Deterministic
    public @This CommandBuilder<H> withDisplayName( final String name ) {

        this.displayName = CommandUtils.validateDisplayName( name );
        return this;

    }


    /**
     * Sets the description of the command.
     * 
     * <p>The default value is {@code null}, e.g. no value. It <i>must</i> be
     * specified before calling {@link #build()} (so this method must be called
     * at least once).
     *
     * @param description The command description.
     * @return This builder.
     * @see Command#description()
     */
    @Deterministic
    public @This CommandBuilder<H> withDescription( final String description ) {

        this.description = CommandUtils.validateDescription( description );
        return this;

    }

    /**
     * Sets the parameters that a user should provide.
     * 
     * <p>The default value is an empty list (no parameters).
     *
     * @param parameters The command parameters. If {@code null}, restores the default
     *                   value.
     * @return This builder.
     * @see Command#parameters()
     */
    @Deterministic
    public @This CommandBuilder<H> withParameters( final @Nullable List<Parameter<?>> parameters ) {

        final var params = new LinkedList<>( 
                Objects.requireNonNullElse( parameters, Collections.emptyList() )
        );
        params.forEach( Objects::requireNonNull );
        this.parameters = params;
        return this;

    }

    /**
     * Adds a parameter to the command.
     *
     * @param parameter The parameter to add.
     * @return This builder.
     * @see Command#parameters()
     */
    @Deterministic
    public @This CommandBuilder<H> addParameter( final Parameter<?> parameter ) {

        this.parameters.add( Objects.requireNonNull( parameter, "Parameter cannot be null." ) );
        return this;

    }

    /**
     * Removes all parameters from the command.
     *
     * @return This builder.
     * @see Command#parameters()
     */
    @Deterministic
    public @This CommandBuilder<H> noParameters() {

        this.parameters.clear();
        return this;

    }

    /**
     * Sets the group that a user must have access for.
     * 
     * <p>The default value is {@link #DEFAULT_GROUP}.
     *
     * @param group The required group. If {@code null}, restores the default value.
     * @return This builder.
     * @see Command#requiredGroup()
     */
    @Deterministic
    public @This CommandBuilder<H> requireGroup( final @Nullable Group group ) {

        this.requiredGroup = Objects.requireNonNullElse( group, DEFAULT_GROUP );
        return this;

    }

    /**
     * Sets whether group access checking should be skipped.
     * 
     * <p>The default value is {@value #DEFAULT_SKIP}, as application commands 
     * should generally avoid clashing with user-set permissions unless necessary.
     *
     * @param skip Whether group access checking should be skipped.
     * @return This builder.
     * @see Command#skipGroupCheckOnInteraction()
     */
    @Deterministic
    public @This CommandBuilder<H> setSkipGroupCheckOnInteraction( final boolean skip ) {

        this.skipGroupCheckOnInteraction = skip;
        return this;

    }

    /**
     * Sets whether the command also require its parent command's groups.
     * 
     * <p>The default value is {@value #DEFAULT_REQUIRE_PARENT_GROUPS}.
     *
     * @param require Whether to require the parent's groups.
     * @return This builder.
     * @see Command#requireParentGroups()
     */
    @Deterministic
    public @This CommandBuilder<H> setRequireParentGroups( final boolean require ) {

        this.requireParentGroups = require;
        return this;

    }

    /**
     * Sets whether the command can only be run in channels marked as NSFW.
     * 
     * <p>The default value is {@value #DEFAULT_NSFW}.
     *
     * @param nsfw Whether the command is NSFW.
     * @return This builder.
     * @see Command#nsfw()
     */
    @Deterministic
    public @This CommandBuilder<H> setNsfw( final boolean nsfw ) {

        this.nsfw = nsfw;
        return this;

    }

    /**
     * Sets whether the command response should be sent privately to the caller.
     * 
     * <p>The default value is {@value #DEFAULT_PRIVATE}.
     *
     * @param privateReply Whether the response should be sent privately.
     * @return This builder.
     * @see Command#privateReply()
     */
    @Deterministic
    public @This CommandBuilder<H> setPrivateReply( final boolean privateReply ) {

        this.privateReply = privateReply;
        return this;

    }

    /**
     * Sets the type of ephemeral response to use, if any.
     * 
     * <p>The default value is {@link #DEFAULT_EPHEMERAL}.
     *
     * @param ephemeralReply The type of ephemeral response to use, if any. 
     *                       If {@code null}, restores the default value.
     * @return This builder.
     * @see Command#ephemeralReply()
     */
    @Deterministic
    public @This CommandBuilder<H> setEphemeralReply( 
            final @Nullable EphemeralType ephemeralReply ) {

        this.ephemeralReply = Objects.requireNonNullElse( ephemeralReply, DEFAULT_EPHEMERAL );
        return this;

    }

    /**
     * Sets whether the command should inherit settings from the parent command.
     * 
     * <p>The default value is {@value #DEFAULT_INHERIT}.
     *
     * @param inheritSettings Whether the command inherits the parent command's settings.
     * @return This builder.
     * @see Command#inheritSettings()
     */
    @Deterministic
    public @This CommandBuilder<H> setInheritSettings( final boolean inheritSettings ) {

        this.inheritSettings = inheritSettings;
        return this;

    }

    /**
     * Sets whether the command should also invoke the parent's handler.
     * 
     * <p>The default value is {@value #DEFAULT_INVOKE_PARENT}.
     *
     * @param invokeParent Whether the command should invoke the parent's handler.
     * @return This builder.
     * @see Command#invokeParent()
     */
    @Deterministic
    public @This CommandBuilder<H> setInvokeParent( final boolean invokeParent ) {

        this.invokeParent = invokeParent;
        return this;

    }

    /**
     * Sets the handlers of the command.
     * 
     * <p>The default value is {@code null}, e.g. no value. It <i>must</i> be
     * specified before calling {@link #build()} (so this method must be called
     * at least once).
     *
     * @param handlers The handlers.
     * @return This builder.
     * @see Command#handlers()
     */
    @Deterministic
    public @This CommandBuilder<H> withHandlers( final H handlers ) {

        this.handlers = CommandUtils.validateHandlers( handlers );
        return this;

    }

    /**
     * Retrieve the name to use for building, after error checking.
     *
     * @return The name to build with.
     * @throws IllegalStateException if {@link #name} was not set.
     */
    @Pure
    private String buildName() throws IllegalStateException {
    
        if ( this.name == null ) {
            throw new IllegalStateException( 
                    "Command name must be set before building." );
        } else {
            return this.name;
        }
    
    }

    /**
     * Retrieve the display name to use for building, after error checking.
     *
     * @return The display name to build with.
     * @throws IllegalStateException if {@link #displayName} was not set.
     */
    @Pure
    private String buildDisplayName() throws IllegalStateException {
    
        if ( this.displayName == null ) {
            throw new IllegalStateException( 
                    "Command display name must be set before building." );
        } else {
            return this.displayName;
        }
    
    }

    /**
     * Retrieve the description to use for building, after error checking.
     *
     * @return The description to build with.
     * @throws IllegalStateException if {@link #description} was not set.
     */
    @Pure
    private String buildDescription() throws IllegalStateException {
    
        if ( this.description == null ) {
            throw new IllegalStateException( 
                    "Command description must be set before building." );
        } else {
            return this.description;
        }
    
    }

    /**
     * Retrieve the handlers to use for building, after error checking.
     *
     * @return The handlers to build with.
     * @throws IllegalStateException if {@link #handlers} was not set.
     */
    @Pure
    private H buildHandlers() throws IllegalStateException {

        if ( this.handlers == null ) {
            throw new IllegalStateException( 
                    "Command handlers must be set before building." );
        } else {
            return this.handlers;
        }

    }

    /**
     * Builds the configured command.
     *
     * @return The built command.
     * @throws IllegalStateException if the current configuration is invalid.
     */
    @SideEffectFree
    public Command<H> build() throws IllegalStateException {

        try {
            return new CommandImpl<>( 
                scope, callable, parent, buildName(), aliases, buildDisplayName(), 
                buildDescription(), parameters,
                requiredGroup, skipGroupCheckOnInteraction, requireParentGroups,
                nsfw, privateReply, ephemeralReply,
                inheritSettings, invokeParent, 
                buildHandlers()
            );
        } catch ( final IllegalArgumentException e ) {
            throw new IllegalStateException( "Invalid parameter configuration.", e );
        }

    }
    
}
