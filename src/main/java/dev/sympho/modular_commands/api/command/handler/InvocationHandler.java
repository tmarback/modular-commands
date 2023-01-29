package dev.sympho.modular_commands.api.command.handler;

import java.util.function.Function;

import org.checkerframework.checker.nullness.qual.NonNull;

import dev.sympho.modular_commands.api.command.context.CommandContext;
import dev.sympho.modular_commands.api.command.result.CommandErrorException;
import dev.sympho.modular_commands.api.command.result.CommandResult;
import dev.sympho.modular_commands.api.command.result.Results;
import dev.sympho.modular_commands.api.exception.ResultException;
import reactor.core.publisher.Mono;

/**
 * A function that handles the execution of a command.
 *
 * @param <C> The context type.
 * @version 1.0
 * @since 1.0
 */
@FunctionalInterface
public interface InvocationHandler<C extends @NonNull CommandContext> 
        extends Function<C, Mono<CommandResult>> {

    // BEGIN BUGGED PARAGRAPH
    /**
     * Handles an invocation of the command.
     *
     * @param context The invocation context.
     * @return The invocation result. It may result in an error (exception) if an
     *         error occured during handling (same as if this handler threw the
     *         exception directly). This includes {@link ResultException}. 
     *         
     *         <p>The returned Mono may also finish empty, in which case handling 
     *         continues with the next handler in the execution chain. Care should
     *         be taken that this only occurs if there <i>is</i> a next handler;
     *         an empty result from the last handler in the chain will cause an
     *         error.
     * @throws ResultException if execution finishes early. This is equivalent to returning 
     *                         the result, but should only be used when returning directly 
     *                         is impractical.
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
    Mono<CommandResult> handle( C context ) throws ResultException, Exception;

    /**
     * Handles an invocation of the command using {@link #handle(CommandContext)}, automatically
     * converting any exception thrown in that method or issued in the resulting mono to an
     * {@link CommandErrorException exception result} (with the exception of a 
     * {@link ResultException}, which is unpacked into the contained result).
     *
     * @param context The invocation context.
     * @return The invocation result. The returned Mono is guaranteed to not contain an error.
     * @implSpec Do not override this.
     */
    @SuppressWarnings( "checkstyle:illegalcatch" )
    default Mono<CommandResult> handleWrapped( final C context ) {

        try {
            return handle( context )
                    .onErrorResume( ResultException.class, e -> Mono.just( e.getResult() ) )
                    .onErrorResume( e -> Results.exceptionMono( e ) );
        } catch ( final ResultException e ) {
            return Mono.just( e.getResult() );
        } catch ( final Exception e ) {
            return Results.exceptionMono( e );
        }

    }

    /**
     * @implSpec Delegates to {@link #handleWrapped(CommandContext)}. Do not override.
     */
    @Override
    default Mono<CommandResult> apply( final C context ) {
        return handleWrapped( context );
    }
    
}
