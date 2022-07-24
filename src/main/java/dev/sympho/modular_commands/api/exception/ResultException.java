package dev.sympho.modular_commands.api.exception;

import dev.sympho.modular_commands.api.command.result.CommandResult;

/**
 * Exception that indicates that command execution finished.
 * 
 * <p>Throwing it from within an execution handler has the same effect as
 * returning the contained result.
 * 
 * <p>Note that, to minimize the runtime overhead of using this exception,
 * it does <i>not</i> record the stack trace or suppressed exceptions.
 *
 * @version 1.0
 * @since 1.0
 * @apiNote This exception is given as an alternative to directly returning the 
 *          result for cases when some early completion occurs deep into the 
 *          call stack, where it would be impractical to return a result normally.
 *          Returning the result directly is preferrable whenever possible.
 */
public class ResultException extends CommandException {

    private static final long serialVersionUID = 8910758904031220818L;

    /** The failure. */
    private final CommandResult result;

    /**
     * Creates a new instance.
     *
     * @param result The result of execution.
     */
    public ResultException( final CommandResult result ) {

        // Minimize runtime overhead
        super( "", null, false, false );

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
