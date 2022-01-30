package dev.sympho.modular_commands.utils.builder.command;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Deterministic;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.Command;
import dev.sympho.modular_commands.api.command.Invocation;
import dev.sympho.modular_commands.api.command.handler.CommandHandler;
import dev.sympho.modular_commands.api.command.handler.ResultHandler;
import dev.sympho.modular_commands.api.command.parameter.Parameter;
import dev.sympho.modular_commands.utils.CommandUtils;
import dev.sympho.modular_commands.utils.builder.Builder;
import discord4j.rest.util.PermissionSet;

/**
 * Base for a command builder.
 *
 * @param <C> The command type.
 * @param <CH> The command handler type.
 * @param <RH> The result handler type.
 * @param <SELF> The self type.
 * @see Command
 * @version 1.0
 * @since 1.0
 */
@SuppressWarnings( "checkstyle:hiddenfield" )
abstract class CommandBuilder<
            C extends @NonNull Command,
            CH extends @NonNull CommandHandler,
            RH extends @NonNull ResultHandler,
            SELF extends @NonNull CommandBuilder<C, CH, RH, SELF>
        > implements Builder<SELF> {

    /** The command parent. */
    protected Invocation parent;

    /** The command name. */
    protected @MonotonicNonNull String name;

    /** The display name. */
    protected @MonotonicNonNull String displayName;

    /** The command description. */
    protected @MonotonicNonNull String description;

    /** The command parameters. */
    protected List<Parameter<?>> parameters;

    /** The required built-in permissions for the user. */
    protected PermissionSet requiredDiscordPermissions;

    /** Whether to also require the parent command's permissions. */
    protected boolean requireParentPermissions;

    /** Whether the command can only be run in NSFW channels. */
    protected boolean nsfw;

    /** Whether the command can only be run by the bot's owner. */
    protected boolean botOwnerOnly;

    /** Whether the command can only be run by the server's owner. */
    protected boolean serverOwnerOnly;

    /** Whether the command response should only be seen by the caller. */
    protected boolean privateReply;

    /** Whether to inherit settings from parent. */
    protected boolean inheritSettings;

    /** The handler to process invocations with. */
    protected @MonotonicNonNull CH invocationHandler;

    /** The handlers to process results with. */
    protected List<RH> resultHandlers;

    /**
     * Constructs a new builder with default values.
     */
    @SideEffectFree
    protected CommandBuilder() {

        this.parent = Invocation.of();
        this.name = null;
        this.displayName = null;
        this.parameters = new LinkedList<>();
        this.requiredDiscordPermissions = PermissionSet.none();
        this.requireParentPermissions = true;
        this.nsfw = false;
        this.botOwnerOnly = false;
        this.serverOwnerOnly = false;
        this.privateReply = true;
        this.inheritSettings = false;
        this.invocationHandler = null;
        this.resultHandlers = new LinkedList<>();

    }

    /**
     * Constructs a new builder that is a copy of the given builder.
     *
     * @param base The builder to copy.
     */
    @SideEffectFree
    protected CommandBuilder( final CommandBuilder<?, ? extends CH, ? extends RH, ?> base ) {

        this.parent = base.parent;
        this.name = base.name;
        this.displayName = base.displayName;
        this.parameters = new LinkedList<>( base.parameters );
        this.requiredDiscordPermissions = base.requiredDiscordPermissions;
        this.requireParentPermissions = base.requireParentPermissions;
        this.nsfw = base.nsfw;
        this.botOwnerOnly = base.botOwnerOnly;
        this.serverOwnerOnly = base.serverOwnerOnly;
        this.privateReply = base.privateReply;
        this.inheritSettings = base.inheritSettings;
        this.invocationHandler = base.invocationHandler;
        this.resultHandlers = new LinkedList<>( base.resultHandlers );

    }

    /**
     * Constructs a new builder that is initialized to make a copy of 
     * the given command.
     *
     * @param base The command to copy.
     * @throws IllegalArgumentException if the given command has invalid values.
     */
    @SideEffectFree
    @SuppressWarnings( "unchecked" )
    protected CommandBuilder( final C base ) throws IllegalArgumentException {

        CommandUtils.validateCommand( base );

        this.parent = base.parent();
        this.name = base.name();
        this.displayName = base.displayName();
        this.parameters = new LinkedList<>( base.parameters() );
        this.requiredDiscordPermissions = base.requiredDiscordPermissions();
        this.requireParentPermissions = base.requireParentPermissions();
        this.nsfw = base.nsfw();
        this.botOwnerOnly = base.botOwnerOnly();
        this.serverOwnerOnly = base.serverOwnerOnly();
        this.privateReply = base.privateReply();
        this.inheritSettings = base.inheritSettings();
        this.invocationHandler = ( CH ) base.invocationHandler();
        this.resultHandlers = new LinkedList<>( ( List<? extends RH> ) base.resultHandlers() );

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
    public SELF withParent( final @Nullable Invocation parent ) {

        this.parent = CommandUtils.validateParent( 
                Objects.requireNonNullElse( parent, Invocation.of() ) );
        return self();

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
    public SELF withName( final String name ) {

        this.name = CommandUtils.validateName( name );
        return self();

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
    public SELF withDisplayName( final String name ) {

        this.displayName = CommandUtils.validateDisplayName( name );
        return self();

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
    public SELF withDescription( final String description ) {

        this.description = CommandUtils.validateDescription( description );
        return self();

    }

    /**
     * Sets the parameters that a user should provide.
     * 
     * <p>The default value is an empty list (no parameters).
     *
     * @param parameters The command parameters. If {@code null}, restores the default
     *                   value.
     * @return This builder.
     * @throws IllegalArgumentException if one of the parameters is not valid.
     * @see Command#parameters()
     */
    @Deterministic
    public SELF withParameters( final @Nullable List<Parameter<?>> parameters )
            throws IllegalArgumentException {

        this.parameters = new LinkedList<>( CommandUtils.validateParameters(
                Objects.requireNonNullElse( parameters, Collections.emptyList() ) ) );
        return self();

    }

    /**
     * Adds a parameter to the command.
     *
     * @param parameter The parameter to add.
     * @return This builder.
     * @see Command#parameters()
     */
    @Deterministic
    public SELF addParameter( final Parameter<?> parameter ) {

        this.parameters.add( Objects.requireNonNull( parameter, "Parameter cannot be null." ) );
        return self();

    }

    /**
     * Removes all parameters from the command.
     *
     * @return This builder.
     * @see Command#parameters()
     */
    @Deterministic
    public SELF noParameters() {

        this.parameters.clear();
        return self();

    }

    /**
     * Sets the built-in permissions that the caller should have.
     * 
     * <p>The default value is an empty set (no permissions required).
     *
     * @param permissions The user permissions. If {@code null}, restores the default
     *                    value.
     * @return This builder.
     * @see Command#requiredDiscordPermissions()
     */
    @Deterministic
    public SELF requireDiscordPermissions( final @Nullable PermissionSet permissions ) {

        this.requiredDiscordPermissions = Objects.requireNonNullElse( permissions, 
                PermissionSet.none() );
        return self();

    }

    /**
     * Sets whether the command also require its parent command's permissions.
     * 
     * <p>The default value is {@code true}.
     *
     * @param require Whether to require the parent's permissions.
     * @return This builder.
     * @see Command#requireParentPermissions()
     */
    @Deterministic
    public SELF setRequireParentPermissions( final boolean require ) {

        this.requireParentPermissions = require;
        return self();

    }

    /**
     * Sets whether the command can only be run in channels marked as NSFW.
     * 
     * <p>The default value is {@code false}.
     *
     * @param nsfw Whether the command is NSFW.
     * @return This builder.
     * @see Command#nsfw()
     */
    @Deterministic
    public SELF setNsfw( final boolean nsfw ) {

        this.nsfw = nsfw;
        return self();

    }

    /**
     * Sets whether the command can only be run by the bot owner.
     * 
     * <p>The default value is {@code false}.
     *
     * @param botOwnerOnly Whether the command can only be used by the bot owner.
     * @return This builder.
     * @see Command#botOwnerOnly()
     */
    @Deterministic
    public SELF setBotOwnerOnly( final boolean botOwnerOnly ) {

        this.botOwnerOnly = botOwnerOnly;
        return self();

    }

    /**
     * Sets whether the command can only be run by the server owner.
     * 
     * <p>The default value is {@code false}.
     *
     * @param serverOwnerOnly Whether the command can only be used by the server owner.
     * @return This builder.
     * @see Command#serverOwnerOnly()
     */
    @Deterministic
    public SELF setServerOwnerOnly( final boolean serverOwnerOnly ) {

        this.serverOwnerOnly = serverOwnerOnly;
        return self();

    }

    /**
     * Sets whether the command response should be sent privately to the caller.
     * 
     * <p>The default value is {@code true}.
     *
     * @param privateReply Whether the response should be sent privately.
     * @return This builder.
     * @see Command#privateReply()
     */
    @Deterministic
    public SELF setPrivateReply( final boolean privateReply ) {

        this.privateReply = privateReply;
        return self();

    }

    /**
     * Sets whether the command should inherit settings from the parent command.
     * 
     * <p>The default value is {@code false}.
     *
     * @param inheritSettings Whether the command inherits the parent command's settings.
     * @return This builder.
     * @see Command#inheritSettings()
     */
    @Deterministic
    public SELF setInheritSettings( final boolean inheritSettings ) {

        this.inheritSettings = inheritSettings;
        return self();

    }

    /**
     * Sets the invocation handler of the command.
     * 
     * <p>The default value is {@code null}, e.g. no value. It <i>must</i> be
     * specified before calling {@link #build()} (so this method must be called
     * at least once).
     *
     * @param handler The handler.
     * @return This builder.
     * @see Command#invocationHandler()
     */
    @Deterministic
    public SELF withInvocationHandler( final CH handler ) {

        this.invocationHandler = CommandUtils.validateInvocationHandler( handler );
        return self();

    }

    /**
     * Sets the handlers that should process the command result before any
     * registry-defined ones.
     * 
     * <p>The default value is an empty list (no handlers).
     *
     * @param handlers The result handlers. If {@code null}, restores the default
     *                 value.
     * @return This builder.
     * @see Command#resultHandlers()
     */
    @Deterministic
    public SELF withResultHandlers( final List<? extends RH> handlers ) {

        this.resultHandlers = new LinkedList<>( CommandUtils.validateResultHandlers(
                Objects.requireNonNullElse( handlers, Collections.emptyList() ) ) );
        return self();

    }

    /**
     * Adds a handler that should process the command result after the previously added
     * handlers but before any registry-defined ones.
     *
     * @param handler The handler to add.
     * @return This builder.
     * @see Command#resultHandlers()
     */
    @Deterministic
    public SELF addResultHandler( final RH handler ) {

        Objects.requireNonNull( handler, "Result handler cannot be null." );
        this.resultHandlers.add( handler );
        return self();

    }

    /**
     * Removes all result handlers (set to no handlers).
     *
     * @return This builder.
     * @see Command#resultHandlers()
     */
    @Deterministic
    public SELF noResultHandlers() {

        this.resultHandlers.clear();
        return self();

    }

    /**
     * Retrieve the name to use for building, after error checking.
     *
     * @return The name to build with.
     * @throws IllegalStateException if {@link #name} was not set.
     */
    protected String buildName() throws IllegalStateException {
    
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
    protected String buildDisplayName() throws IllegalStateException {
    
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
    protected String buildDescription() throws IllegalStateException {
    
        if ( this.description == null ) {
            throw new IllegalStateException( 
                    "Command description must be set before building." );
        } else {
            return this.description;
        }
    
    }

    /**
     * Retrieve the invocation handler to use for building, after error checking.
     *
     * @return The invocation handler to build with.
     * @throws IllegalStateException if {@link #invocationHandler} was not set.
     */
    protected CH buildInvocationHandler() throws IllegalStateException {
    
        if ( this.invocationHandler == null ) {
            throw new IllegalStateException( 
                    "Command invocation handler must be set before building." );
        } else {
            return this.invocationHandler;
        }
    
    }

    /**
     * Builds the configured command.
     *
     * @return The built command.
     * @throws IllegalStateException if the current configuration is invalid.
     */
    @SideEffectFree
    public abstract C build() throws IllegalStateException;
    
}
