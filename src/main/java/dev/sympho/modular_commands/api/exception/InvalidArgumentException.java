package dev.sympho.modular_commands.api.exception;

import dev.sympho.modular_commands.api.context.Parameter;

/**
 * Exception type for errors caused due to invalid arguments being passed
 * to a command.
 *
 * @version 1.0
 * @since 1.0
 */
public class InvalidArgumentException extends CommandException {
    
    /** Serial UID. */
    private static final long serialVersionUID = 3840587706158373045L;

    /** The parameter that triggered the error. */
    private final Parameter<?> parameter;

    /**
     * Constructs a new exception.
     *
     * @param parameter The parameter that triggered the error.
     * @param message A message detailing why the argument is invalid.
     */
    public InvalidArgumentException( final Parameter<?> parameter, final String message ) {

        super( message );
        this.parameter = parameter;

    }

    /**
     * Constructs a new exception.
     *
     * @param parameter The parameter that triggered the error.
     * @param message A message detailing why the argument is invalid.
     * @param cause The exception that caused the value to be invalid.
     */
    public InvalidArgumentException( final Parameter<?> parameter, final String message,
            final Throwable cause ) {

        super( message, cause );
        this.parameter = parameter;

    }

    /**
     * Retrieves the parameter for the argument that caused the exception.
     *
     * @return The parameter that triggered the error.
     */
    public Parameter<?> getParameter() {

        return parameter;

    }

}
