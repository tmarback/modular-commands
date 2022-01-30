package dev.sympho.modular_commands.utils.builder.command;

import java.util.Set;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Deterministic;

import dev.sympho.modular_commands.api.command.MessageCommand;
import dev.sympho.modular_commands.utils.builder.Builder;

/**
 * Extra methods for setting parameters of variants of {@link MessageCommand}.
 *
 * @param <SELF> The self type.
 * @version 1.0
 * @since 1.0
 * @apiNote This only exists because multiple class inheritance isn't a thing in Java and
 *          at least I don't want to write the same Javadoc for every builder that makes 
 *          a derivative of MessageCommand.
 */
interface MessageCommandBuilderMethods<SELF extends CommandBuilder<?, ?, ?, SELF>> 
        extends Builder<SELF> {

    /**
     * Sets the aliases that a user should support.
     * 
     * <p>The default value is an empty set (no aliases).
     *
     * @param aliases The command aliases. If {@code null}, restores the default
     *                value.
     * @return This builder.
     * @throws IllegalArgumentException if one of the aliases is not valid.
     * @see MessageCommand#aliases()
     */
    @Deterministic
    SELF withAliases( @Nullable Set<String> aliases ) throws IllegalArgumentException;

    /**
     * Adds an alias to the command.
     *
     * @param alias The alias to add.
     * @return This builder.
     * @throws IllegalArgumentException if the alias is not valid.
     * @see MessageCommand#aliases()
     */
    @Deterministic
    SELF addAliases( String alias ) throws IllegalArgumentException;

    /**
     * Removes all aliases from the command.
     *
     * @return This builder.
     * @see MessageCommand#aliases()
     */
    @Deterministic
    SELF noAliases();
    
}
