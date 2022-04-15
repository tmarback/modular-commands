package dev.sympho.modular_commands.api.command;

import java.util.Collections;
import java.util.List;

import dev.sympho.modular_commands.api.command.handler.AnyInvocationHandler;
import dev.sympho.modular_commands.api.command.handler.AnyResultHandler;

/**
 * A command that is invoked through any form of text (as opposed to a GUI element
 * like a button).
 *
 * @version 1.0
 * @since 1.0
 */
public interface TextCommand extends MessageCommand, SlashCommand {

    @Override
    AnyInvocationHandler invocationHandler();

    @Override
    default List<? extends AnyResultHandler> resultHandlers() {
        return Collections.emptyList();
    }
    
}
