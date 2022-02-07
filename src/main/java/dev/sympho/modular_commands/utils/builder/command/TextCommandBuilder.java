package dev.sympho.modular_commands.utils.builder.command;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.TextCommand;
import dev.sympho.modular_commands.api.command.handler.AnyCommandHandler;
import dev.sympho.modular_commands.api.command.handler.AnyResultHandler;
import dev.sympho.modular_commands.impl.command.TextCommandImpl;
import dev.sympho.modular_commands.utils.CommandUtils;

/**
 * A builder for a message command.
 *
 * @version 1.0
 * @since 1.0
 */
@SuppressWarnings( "checkstyle:hiddenfield" )
public final class TextCommandBuilder extends CommandBuilder<TextCommand, 
        AnyCommandHandler, AnyResultHandler, TextCommandBuilder> 
        implements MessageCommandBuilderMethods<TextCommandBuilder> {

    /** The command aliases. */
    protected Set<String> aliases;

    /**
     * Constructs a new builder with default values.
     */
    @SideEffectFree
    public TextCommandBuilder() {

        this.aliases = new HashSet<>();

    }

    /**
     * Constructs a new builder that is a copy of the given builder.
     *
     * @param base The builder to copy.
    */
    @SideEffectFree
    public TextCommandBuilder( final TextCommandBuilder base ) {
        super( base );

        this.aliases = new HashSet<>( base.aliases );

    }

    /**
     * Constructs a new builder that is initialized to make a copy of 
     * the given command.
     *
     * @param base The command to copy.
     * @throws IllegalArgumentException if the given command has invalid values.
     */
    @SideEffectFree
    public TextCommandBuilder( final TextCommand base ) throws IllegalArgumentException {
        super( base );

        this.aliases = new HashSet<>( base.aliases() );

    }

    @Override
    public TextCommandBuilder withAliases( final @Nullable Set<String> aliases ) 
            throws IllegalArgumentException {

        this.aliases = new HashSet<>( CommandUtils.validateAliases(
                Objects.requireNonNullElse( aliases, Collections.emptySet() ) ) );
        return self();

    }

    @Override
    public TextCommandBuilder addAliases( final String alias ) 
            throws IllegalArgumentException {

        this.aliases.add( CommandUtils.validateAlias( alias ) );
        return self();

    }

    @Override
    public TextCommandBuilder noAliases() {

        this.aliases.clear();
        return self();

    }

    @Override
    public TextCommand build() throws IllegalStateException {

        try {
            return new TextCommandImpl( 
                scope, callable, parent, buildName(), buildDisplayName(), aliases,
                buildDescription(), parameters,
                requiredDiscordPermissions, requireParentPermissions, 
                nsfw, botOwnerOnly, serverOwnerOnly, privateReply, 
                inheritSettings, invokeParent,
                buildInvocationHandler(), resultHandlers );
        } catch ( final IllegalArgumentException e ) {
            throw new IllegalStateException( "Invalid parameter configuration.", e );
        }

    }
    
}
