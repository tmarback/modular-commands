package dev.sympho.modular_commands.api.command.parameter.parse;

import org.checkerframework.checker.nullness.util.NullnessUtil;

import dev.sympho.modular_commands.api.exception.CommandException;

/**
 * Exception thrown when an argument cannot be parsed due to being invalid.
 *
 * @version 1.0
 * @since 1.0
 */
public class InvalidArgumentException extends CommandException {

    private static final long serialVersionUID = -6587869519938247026L;

    /**
     * Constructs a new exception.
     *
     * @param message A message detailing why the argument is invalid.
     */
    public InvalidArgumentException( final String message ) {

        super( message );

    }

    /**
     * Constructs a new exception.
     *
     * @param message A message detailing why the argument is invalid.
     * @param cause The exception that caused the value to be invalid.
     */
    public InvalidArgumentException( final String message, final Throwable cause ) {

        super( message, cause );

    }

    // Just to narrow the return to NonNull.
    @Override
    public String getMessage() {
        return NullnessUtil.castNonNull( super.getMessage() );
    }
    
}
