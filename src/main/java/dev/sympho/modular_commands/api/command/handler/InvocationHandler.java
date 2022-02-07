package dev.sympho.modular_commands.api.command.handler;

import dev.sympho.modular_commands.api.command.context.AnyCommandContext;
import dev.sympho.modular_commands.api.command.result.CommandErrorException;
import dev.sympho.modular_commands.api.command.result.CommandResult;
import reactor.core.publisher.Mono;

/**
 * A function that handles the execution of a command.
 *
 * @version 1.0
 * @since 1.0
 */
public sealed interface InvocationHandler 
        permits MessageInvocationHandler, SlashInvocationHandler {

    // BEGIN BUGGED PARAGRAPH
    /**
     * Handles an invocation of the command.
     * 
     * <p>If this handler throws an exception, or the returned Mono results into an exception,
     * the exception is automatically converted into an 
     * {@link CommandErrorException exception result}.
     *
     * @param context The invocation context.
     * @return The invocation result. It may result in an error (exception) if an
     *         error occured during handling (same as if this handler threw an
     *         exception directly).
     * @throws Exception if an error occurred during handling.
     * @apiNote A handler is allowed to throw checked exceptions in order to simplify 
     *          error handling. If an exception occurs, the usual course of action would
     *          be to return it as an {@link CommandErrorException exception result}.
     *          Thus, it makes more sense to allow it to propagate up, then have the
     *          centralized handler automatically wrap the exception into an error,
     *          rather than clutter every handler with similar exception handling just
     *          for that purpose.
     * 
     *          <p>If an exception requires handling to avoid leaving the system in an
     *          inconsistent or dangerous state, it may be implemented either in the
     *          handler itself, or in a {@link ResultHandler} that detects and handles
     *          the resulting error.
     * 
     *          <p>The same logic applies to the returned Mono being allowed to result in
     *          an error. Of course, in that case there is no way to allow checked
     *          exceptions, so those would still need to be wrapped into an unchecked
     *          exception or manually returned as an exception result.
     */
    // END BUGGED PARAGRAPH
    Mono<CommandResult> handle( AnyCommandContext context ) throws Exception;
    
}
