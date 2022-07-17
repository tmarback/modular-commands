package dev.sympho.modular_commands.api.command;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.checkerframework.dataflow.qual.Pure;

import dev.sympho.modular_commands.api.command.handler.MessageInvocationHandler;
import dev.sympho.modular_commands.api.command.handler.MessageResultHandler;

/**
 * A message-based command.
 *
 * @version 1.0
 * @since 1.0
 */
public non-sealed interface MessageCommand extends Command {

    /**
     * The aliases that may also invoke the command.
     * 
     * <p>These aliases are only relevant to handling invocations. They may not be
     * used to specify the command as a parent.
     * 
     * <p>Aliases must satisfy the same restrictions as the {@link #name() name}.
     *
     * @return The command aliases.
     */
    @Pure
    Set<String> aliases();

    /**
     * The alias invocations that may also invoke the command.
     *
     * @return The command aliases as invocations.
     * @see #aliases()
     * @implSpec The invocation is determined by appending each alias to the 
     *           {@link #parent() parent}.
     */
    @Pure
    default Set<Invocation> aliasInvocations() {

        return aliases().stream()
                .map( this.parent()::child )
                .collect( Collectors.toUnmodifiableSet() );

    }

    @Override
    MessageInvocationHandler invocationHandler();

    @Override
    default List<? extends MessageResultHandler> resultHandlers() {
        return Collections.emptyList();
    }
    
}
