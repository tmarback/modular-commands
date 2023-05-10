package dev.sympho.modular_commands.api.command.parameter.parse;

import java.util.Objects;

import org.checkerframework.checker.nullness.util.NullnessUtil;

import dev.sympho.modular_commands.api.exception.CommandException;

/**
 * Exception raised when fetching an attachment argument fails.
 *
 * @version 1.0
 * @since 1.0
 */
public class AttachmentFetchFailedException extends CommandException {

    private static final long serialVersionUID = 312091641026973467L;

    /**
     * Creates a new instance.
     *
     * @param message The error message.
     */
    public AttachmentFetchFailedException( final String message ) {

        super( Objects.requireNonNull( message ) );

    }

    // Just to narrow the return to NonNull.
    @Override
    public String getMessage() {
        return NullnessUtil.castNonNull( super.getMessage() );
    }
    
}
