package dev.sympho.modular_commands.api.command.result;

/**
 * The result from a command that fails to execute due to user error.
 * 
 * <p>This is not the result for a command that fails due to an internal
 * error. {@link CommandError} is used in that case.
 *
 * @version 1.0
 * @since 1.0
 */
public non-sealed interface CommandFailure extends CommandResult {}
