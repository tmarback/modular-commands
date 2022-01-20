package dev.sympho.modular_commands.api.command.result;

/**
 * The result of a command.
 *
 * @version 1.0
 * @since 1.0
 */
public sealed interface CommandResult 
        permits CommandContinue, CommandSuccess, CommandFailure, CommandError {}
