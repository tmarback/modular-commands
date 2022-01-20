package dev.sympho.modular_commands.api.command.result;

import org.checkerframework.dataflow.qual.Pure;

/**
 * The result from a command that fails to execute due to an internal error.
 * 
 * <p>This is not the result for a command that fails due to user
 * error. {@link CommandFailure} is used in that case.
 *
 * @version 1.0
 * @since 1.0
 */
public non-sealed interface CommandError extends CommandResult {

    /**
     * Retrieves the error message.
     *
     * @return The error message.
     */
    @Pure
    String message();
    
}
