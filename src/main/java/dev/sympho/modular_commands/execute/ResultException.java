package dev.sympho.modular_commands.execute;

import dev.sympho.modular_commands.api.command.result.CommandContinue;
import dev.sympho.modular_commands.api.command.result.CommandResult;

/**
 * Exception type that indicates that command execution has completed.
 *
 * @version 1.0
 * @since 1.0
 */
public class ResultException extends RuntimeException {

    private static final long serialVersionUID = -71417643583700194L;

    /** The execution result. */
    private final CommandResult result;

    /**
     * Creates a new instance.
     *
     * @param result The result of execution. The result must indicate a completed
     *               execution, so a {@link CommandContinue continue result} is not
     *               allowed.
     * @throws IllegalArgumentException If the given result is of type {@link CommandContinue}.
     */
    public ResultException( final CommandResult result ) throws IllegalArgumentException {

        if ( result instanceof CommandContinue ) {
            throw new IllegalArgumentException( "Continue result not allowed." );
        }

        this.result = result;

    }

    /**
     * Retrieves the execution result.
     *
     * @return The execution result.
     */
    public CommandResult getResult() {

        return result;

    }
    
}
