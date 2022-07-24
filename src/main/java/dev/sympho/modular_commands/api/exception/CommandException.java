package dev.sympho.modular_commands.api.exception;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Base type for exceptions related to the command system.
 *
 * @version 1.0
 * @since 1.0
 */
public class CommandException extends RuntimeException {

    /** Serial UID. */
    private static final long serialVersionUID = 4017640033490631277L;

    /**
     * Constructs a new exception.
     *
     * @see RuntimeException#RuntimeException()
     */
    public CommandException() {}

    /**
     * Constructs a new exception.
     *
     * @param message The detail message.
     * @see RuntimeException#RuntimeException(String)
     */
    public CommandException( final String message ) {
        super( message );
    }

    /**
     * Constructs a new exception.
     *
     * @param message The detail message.
     * @param cause The cause.
     * @see RuntimeException#RuntimeException(String, Throwable)
     */
    public CommandException( final String message, final Throwable cause ) {
        super( message, cause );
    }

    /**
     * Constructs a new exception.
     *
     * @param cause The cause.
     * @see RuntimeException#RuntimeException(Throwable)
     */
    public CommandException( final Throwable cause ) {
        super( cause );
    }
    
    /**
     * Constructs a new exception.
     *
     * @param message The detail message.
     * @param cause The cause. May be {@code null}.
     * @param enableSuppression Whether or not suppression is enabled or disabled.
     * @param writableStackTrace Whether or not the stack trace should be writable
     * @see RuntimeException#RuntimeException(String, Throwable, boolean, boolean)
     */
    protected CommandException( final String message, final @Nullable Throwable cause,
            final boolean enableSuppression, final boolean writableStackTrace ) {
        super( message, cause, enableSuppression, writableStackTrace );
    }
    
}
