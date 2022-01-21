package dev.sympho.modular_commands.api.command;

import java.util.Collections;
import java.util.List;

import dev.sympho.modular_commands.api.command.handler.SlashCommandHandler;
import dev.sympho.modular_commands.api.command.handler.SlashResultHandler;

/**
 * A slash command.
 *
 * @version 1.0
 * @since 1.0
 */
public non-sealed interface SlashCommand extends Command {

    @Override
    SlashCommandHandler invocationHandler();

    @Override
    default List<? extends SlashResultHandler> resultHandlers() {
        return Collections.emptyList();
    }

}
