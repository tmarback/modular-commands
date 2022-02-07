package dev.sympho.modular_commands.api.command.result;

/**
 * Failure result due to an issue with the provided arguments.
 *
 * @version 1.0
 * @since 1.0
 */
public sealed interface CommandFailureArgument extends CommandFailureMessage 
        permits CommandFailureArgumentExtra, CommandFailureArgumentMissing, 
            CommandFailureArgumentInvalid {}
