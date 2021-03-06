package dev.sympho.modular_commands.api.command;

import java.util.Collections;
import java.util.List;

import dev.sympho.modular_commands.api.command.handler.InteractionInvocationHandler;
import dev.sympho.modular_commands.api.command.handler.InteractionResultHandler;

/**
 * An interaction-based command.
 *
 * @version 1.0
 * @since 1.0
 */
public interface InteractionCommand extends SlashCommand {

    @Override
    InteractionInvocationHandler invocationHandler();

    @Override
    default List<? extends InteractionResultHandler> resultHandlers() {
        return Collections.emptyList();
    }
    
}
