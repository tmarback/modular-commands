package dev.sympho.modular_commands.api.exception;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.checkerframework.dataflow.qual.Pure;

import dev.sympho.modular_commands.api.command.Command;
import dev.sympho.modular_commands.api.command.Invocation;
import dev.sympho.modular_commands.api.command.result.CommandContinue;
import dev.sympho.modular_commands.execute.InvocationUtils;

/**
 * Exception type that is triggered when the invocation handling of a command is completed
 * without a non-{@link CommandContinue continue} result.
 *
 * @version 1.0
 * @since 1.0
 */
public class IncompleteHandlingException extends CommandException {

    private static final long serialVersionUID = -6242043843932336667L;

    /** The command chain that was being executed. */
    private final List<Command> chain;

    /** The invocation that triggered the command. */
    private final Invocation invocation;

    /**
     * Creates a new instance.
     *
     * @param chain The command that was being executed.
     * @param invocation The invocation that triggered the command.
     */
    public IncompleteHandlingException( final List<? extends Command> chain,
            final Invocation invocation ) {

        super( String.format( "Command %s under invocation %s was not completely handled.",
                InvocationUtils.getInvokedCommand( chain ).invocation(), invocation ) );

        this.chain = Collections.unmodifiableList( new ArrayList<>( chain ) );
        this.invocation = invocation;
        
    }

    /**
     * Retrieves the command that was invoked.
     *
     * @return The command that was invoked.
     */
    @Pure
    public Command getCommand() {
        return InvocationUtils.getInvokedCommand( chain );
    }

    /**
     * Retrieves the command chain that was being executed.
     *
     * @return The command chain that was being executed.
     */
    @Pure
    public List<Command> getExecutionChain() {
        return chain;
    }

    /**
     * Retrieves the invocation that triggered the command.
     *
     * @return The invocation that triggered the command.
     */
    @Pure
    public Invocation getInvocation() {
        return invocation;
    }
    
}
