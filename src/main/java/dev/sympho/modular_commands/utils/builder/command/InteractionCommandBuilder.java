package dev.sympho.modular_commands.utils.builder.command;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.dataflow.qual.Deterministic;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.Command;
import dev.sympho.modular_commands.api.command.handler.InvocationHandler;
import dev.sympho.modular_commands.api.command.handler.ResultHandler;

/**
 * Base for an interaction-based command builder.
 *
 * @param <C> The command type.
 * @param <IH> The command handler type.
 * @param <RH> The result handler type.
 * @param <SELF> The self type.
 * @see Command
 * @version 1.0
 * @since 1.0
 */
abstract class InteractionCommandBuilder<
            C extends @NonNull Command,
            IH extends @NonNull InvocationHandler,
            RH extends @NonNull ResultHandler,
            SELF extends @NonNull InteractionCommandBuilder<C, IH, RH, SELF>
        > extends CommandBuilder<C, IH, RH, SELF> {

    /** Default for {@link #setSkipGroupCheckOnInteraction(boolean)}. */
    public static final boolean DEFAULT_SKIP = true;

    /** Whether group access checking should be skipped. */
    boolean skipGroupCheckOnInteraction;

    /**
     * Constructs a new builder with default values.
     */
    @SideEffectFree
    public InteractionCommandBuilder() {

        this.skipGroupCheckOnInteraction = DEFAULT_SKIP;

    }

    /**
     * Constructs a new builder that is a copy of the given builder.
     *
     * @param base The builder to copy.
    */
    @SideEffectFree
    public InteractionCommandBuilder( 
            final InteractionCommandBuilder<?, ? extends IH, ? extends RH, ?> base ) {
        super( base );

        this.skipGroupCheckOnInteraction = base.skipGroupCheckOnInteraction;

    }

    /**
     * Constructs a new builder that is initialized to make a copy of 
     * the given command.
     *
     * @param base The command to copy.
     * @throws IllegalArgumentException if the given command has invalid values.
     */
    @SideEffectFree
    public InteractionCommandBuilder( final C base ) 
            throws IllegalArgumentException {
        super( base );

        this.skipGroupCheckOnInteraction = base.skipGroupCheckOnInteraction();

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
    public SELF setSkipGroupCheckOnInteraction( final boolean skip ) {

        this.skipGroupCheckOnInteraction = skip;
        return self();

    }
    
}
