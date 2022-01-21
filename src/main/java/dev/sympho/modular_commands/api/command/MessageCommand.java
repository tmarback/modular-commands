package dev.sympho.modular_commands.api.command;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.checkerframework.dataflow.qual.Pure;

import dev.sympho.modular_commands.api.command.handler.MessageCommandHandler;
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
     * @return The command aliases.
     */
    @Pure
    Set<String> aliases();

    @Override
    MessageCommandHandler invocationHandler();

    @Override
    default List<? extends MessageResultHandler> resultHandlers() {
        return Collections.emptyList();
    }
    
}
