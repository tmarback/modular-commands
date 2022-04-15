package dev.sympho.modular_commands.api.exception;

import dev.sympho.modular_commands.api.command.result.CommandFailure;

/**
 * Exception that indicates that command execution failed.
 * 
 * <p>Throwing it from within an execution handler has the same effect as
 * returning the contained failure result.
 *
 * @version 1.0
 * @since 1.0
 * @apiNote This exception is given as an alternative to directly returning the 
 *          failure result for cases when the failure occurs deep into the 
 *          call stack, where it would be impractical to return a result normally.
 *          Returning the failure directly is preferrable whenever possible.
 */
public class FailureException extends CommandException {

    private static final long serialVersionUID = 8910758904031220818L;

    /** The failure. */
    private final CommandFailure result;

    /**
     * Creates a new instance.
     *
     * @param result The result of execution.
     */
    public FailureException( final CommandFailure result ) {

        this.result = result;

    }

    /**
     * Retrieves the execution result.
     *
     * @return The execution result.
     */
    public CommandFailure getResult() {

        return result;

    }
    
}
