package dev.sympho.modular_commands.api.command.result;

import org.checkerframework.dataflow.qual.Pure;

/**
 * A successful result with a message to the user.
 *
 * @version 1.0
 * @since 1.0
 */
public interface CommandSuccessMessage extends CommandSuccess {

    /**
     * Retrieves the message to the user.
     *
     * @return The message to the user.
     */
    @Pure
    String message();
    
}
