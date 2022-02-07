package dev.sympho.modular_commands.api.exception;

import dev.sympho.modular_commands.api.command.Command;
import dev.sympho.modular_commands.api.command.Invocation;

/**
 * Exception type that is triggered when a command chain being invoked is not valid due to
 * an incompatiblity between commands in the chain.
 *
 * @version 1.0
 * @since 1.0
 */
public class InvalidChainException extends CommandException {

    private static final long serialVersionUID = 6191638556464908251L;

    /** The command that was being executed. */
    private final Invocation command;

    /** The ancestor that is incompatible. */
    private final Invocation parent;

    /**
     * Creates a new instance.
     *
     * @param command The command that was being executed.
     * @param parent The ancestor that is incompatible.
     * @param message The error message.
     */
    public InvalidChainException( final Command command, final Command parent, 
            final String message ) {

        super( message );

        this.command = command.invocation();
        this.parent = parent.invocation();

    }

    /**
     * Retrieves the command that was being executed.
     *
     * @return The command that was being executed.
     */
    public Invocation getCommand() {
        return command;
    }

    /**
     * Retrieves the ancestor that is incompatible.
     *
     * @return The ancestor that is incompatible.
     */
    public Invocation getParent() {
        return parent;
    }

    @Override
    public String toString() {

        return String.format( "Mismatch in parent %s for command %s: %s", 
                getParent(), getCommand(), getMessage() );

    }
    
}
