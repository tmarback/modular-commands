package dev.sympho.modular_commands.api.command.result;

/**
 * Result that indicates that the execution chain should continue.
 * 
 * <p>This result is only valid for handlers that are part of a larger execution chain
 * (have parent or child) handlers executing after it. Using it in any other context
 * will result in an error.
 *
 * @version 1.0
 * @since 1.0
 */
public sealed interface CommandContinue extends CommandResult permits Results.ResultContinue {}
