package dev.sympho.modular_commands.utils.builder.command;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.MessageCommand;
import dev.sympho.modular_commands.api.command.handler.MessageInvocationHandler;
import dev.sympho.modular_commands.api.command.handler.MessageResultHandler;
import dev.sympho.modular_commands.impl.command.MessageCommandImpl;
import dev.sympho.modular_commands.utils.CommandUtils;

/**
 * A builder for a message command.
 *
 * @version 1.0
 * @since 1.0
 */
@SuppressWarnings( "checkstyle:hiddenfield" )
public final class MessageCommandBuilder extends CommandBuilder<MessageCommand, 
        MessageInvocationHandler, MessageResultHandler, MessageCommandBuilder> 
        implements MessageCommandBuilderMethods<MessageCommandBuilder> {

    /** The command aliases. */
    protected Set<String> aliases;

    /**
     * Constructs a new builder with default values.
     */
    @SideEffectFree
    public MessageCommandBuilder() {

        this.aliases = new HashSet<>();

    }

    /**
     * Constructs a new builder that is a copy of the given builder.
     *
     * @param base The builder to copy.
    */
    @SideEffectFree
    public MessageCommandBuilder( final MessageCommandBuilder base ) {
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
    public MessageCommandBuilder( final MessageCommand base ) throws IllegalArgumentException {
        super( base );

        this.aliases = new HashSet<>( base.aliases() );

    }

    @Override
    public MessageCommandBuilder withAliases( final @Nullable Set<String> aliases ) 
            throws IllegalArgumentException {

        this.aliases = new HashSet<>( CommandUtils.validateAliases(
                Objects.requireNonNullElse( aliases, Collections.emptySet() ) ) );
        return self();

    }

    @Override
    public MessageCommandBuilder addAliases( final String alias ) 
            throws IllegalArgumentException {

        this.aliases.add( CommandUtils.validateAlias( alias ) );
        return self();

    }

    @Override
    public MessageCommandBuilder noAliases() {

        this.aliases.clear();
        return self();

    }

    @Override
    public MessageCommand build() throws IllegalStateException {

        try {
            return new MessageCommandImpl( 
                parent, buildName(), buildDisplayName(), aliases, buildDescription(), parameters,
                requiredDiscordPermissions, requireParentPermissions, 
                nsfw, botOwnerOnly, serverOwnerOnly, privateReply, 
                inheritSettings, buildInvocationHandler(), resultHandlers );
        } catch ( final IllegalArgumentException e ) {
            throw new IllegalStateException( "Invalid parameter configuration.", e );
        }

    }
    
}
