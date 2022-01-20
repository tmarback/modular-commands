package dev.sympho.modular_commands.api.command.result;

import java.util.Objects;

import org.checkerframework.dataflow.qual.Pure;

/**
 * An error result due to an exception being thrown.
 *
 * @version 1.0
 * @since 1.0
 */
public interface CommandErrorException extends CommandError {

    /**
     * Retrieves the cause of the error.
     *
     * @return The cause.
     */
    @Pure
    Throwable cause();

    /**
     * {@inheritDoc}
     *
     * @implSpec The default behavior is to return the {@link Throwable#getMessage() message}
     *           provided by the {@link #cause() exception}.
     */
    @Override
    default String message() {

        return Objects.requireNonNullElse( cause().getMessage(), "An exception was thrown." );

    }
    
}
