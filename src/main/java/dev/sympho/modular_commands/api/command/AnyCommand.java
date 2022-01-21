package dev.sympho.modular_commands.api.command;

import java.util.Collections;
import java.util.List;

import dev.sympho.modular_commands.api.command.handler.AnyCommandHandler;
import dev.sympho.modular_commands.api.command.handler.AnyResultHandler;

/**
 * A command that may be invoked through any supported form.
 *
 * @version 1.0
 * @since 1.0
 */
public interface AnyCommand extends MessageCommand, SlashCommand {

    @Override
    AnyCommandHandler invocationHandler();

    @Override
    default List<? extends AnyResultHandler> resultHandlers() {
        return Collections.emptyList();
    }
    
}
