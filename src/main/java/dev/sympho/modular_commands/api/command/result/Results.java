package dev.sympho.modular_commands.api.command.result;

import reactor.core.publisher.Mono;

/**
 * Utilities for generating results from handlers.
 *
 * @version 1.0
 * @since 1.0
 */
public final class Results {

    /** 
     * Result indicating execution should continue along the chain.
     *
     * @see #cont()
     */
    public static final CommandContinue CONTINUE = new ResultContinue();

    /** 
     * Result indicating the command executed successfully with no further context. 
     *
     * @see #ok()
     */
    public static final CommandSuccess OK = new ResultOK();

    /** 
     * Result indicating the command failed with no further context. 
     *
     * @see #fail()
     */
    public static final CommandFailure FAIL = new ResultFail();

    /** Do not instantiate. */
    private Results() {}

    /**
     * Generates a result indicating execution should continue along the chain.
     *
     * @return The generated result.
     * @apiNote Would have been named {@code continue()} but that's a reserved
     *          keyword.
     */
    public static CommandContinue cont() {

        return CONTINUE;

    }

    /**
     * Generates a result indicating execution should continue along the chain.
     *
     * @return A Mono that issues the generated result.
     * @see #cont()
     */
    public static Mono<CommandResult> contMono() {

        return Mono.just( cont() );

    }

    /**
     * Generates a result indicating the command executed successfully with no further context.
     *
     * @return The generated result.
     */
    public static CommandSuccess ok() {

        return OK;

    }

    /**
     * Generates a result indicating the command executed successfully with no further context.
     *
     * @return A Mono that issues the generated result.
     * @see #ok()
     */
    public static Mono<CommandResult> okMono() {

        return Mono.just( ok() );

    }

    /**
     * Generates a result indicating the command failed with no further context.
     *
     * @return The generated result.
     */
    public static CommandFailure fail() {

        return FAIL;

    }

    /**
     * Generates a result indicating the command failed with no further context.
     *
     * @return A Mono that issues the generated result.
     * @see #fail()
     */
    public static Mono<CommandResult> failMono() {

        return Mono.just( fail() );

    }

    /**
     * Generates a result indicating the command executed successfully with a message
     * to the user.
     *
     * @param message The message to the user.
     * @return The generated result.
     */
    public static CommandSuccessMessage success( final String message ) {

        return new ResultSuccessMessage( message );

    }

    /**
     * Generates a result indicating the command executed successfully with a message
     * to the user.
     *
     * @param message The message to the user.
     * @return A Mono that issues the generated result.
     * @see #success(String)
     */
    public static Mono<CommandResult> successMono( final String message ) {

        return Mono.just( success( message ) );

    }

    /**
     * Generates a result indicating the command failed with a message to the user.
     *
     * @param message The message to the user.
     * @return The generated result.
     */
    public static CommandFailureMessage failure( final String message ) {

        return new ResultFailureMessage( message );

    }

    /**
     * Generates a result indicating the command failed with a message to the user.
     *
     * @param message The message to the user.
     * @return A Mono that issues the generated result.
     * @see #failure(String)
     */
    public static Mono<CommandResult> failureMono( final String message ) {

        return Mono.just( failure( message ) );

    }

    /**
     * Generates a result indicating the command encountered an error.
     *
     * @param message The error message.
     * @return The generated result.
     */
    public static CommandError error( final String message ) {

        return new ResultError( message );

    }

    /**
     * Generates a result indicating the command encountered an error.
     *
     * @param message The error message.
     * @return A Mono that issues the generated result.
     * @see #error(String)
     */
    public static Mono<CommandResult> errorMono( final String message ) {

        return Mono.just( error( message ) );

    }

    /**
     * Generates a result indicating the command encountered an error due to an exception.
     *
     * @param cause The exception that caused the error.
     * @return The generated result.
     * @apiNote Exceptions thrown by a command handler are automatically wrapped into an
     *          error result. As such, allowing the exception to propagate up is preferrable
     *          to explicitly generating the error.
     */
    public static CommandErrorException exception( final Throwable cause ) {

        return new ResultException( cause );

    }

    /**
     * Generates a result indicating the command encountered an error due to an exception.
     *
     * @param cause The exception that caused the error.
     * @return A Mono that issues the generated result.
     * @see #exception(Throwable)
     */
    public static Mono<CommandResult> exceptionMono( final Throwable cause ) {

        return Mono.just( exception( cause ) );

    }

    /* Implementations for the returns */

    /** 
     * Implementation for {@link Results#cont()}. 
     *
     * @version 1.0
     * @since 1.0
     */
    record ResultContinue() implements CommandContinue {}

    /** 
     * Implementation for {@link Results#ok()}. 
     *
     * @version 1.0
     * @since 1.0
     */
    record ResultOK() implements CommandSuccess {}

    /** 
     * Implementation for {@link Results#fail()}. 
     *
     * @version 1.0
     * @since 1.0
     */
    record ResultFail() implements CommandFailure {}

    /**
     * Implementation for {@link Results#success(String)}.
     *
     * @param message The message to the user.
     * @version 1.0
     * @since 1.0
     */
    record ResultSuccessMessage( String message ) implements CommandSuccessMessage {}

    /**
     * Implementation for {@link Results#failure(String)}.
     *
     * @param message The message to the user.
     * @version 1.0
     * @since 1.0
     */
    record ResultFailureMessage( String message ) implements CommandFailureMessage {}

    /**
     * Implementation for {@link Results#error(String)}.
     *
     * @param message The error message.
     * @version 1.0
     * @since 1.0
     */
    record ResultError( String message ) implements CommandError {}

    /**
     * Implementation for {@link Results#exception(Throwable)}.
     *
     * @param cause The exception that caused the error.
     * @version 1.0
     * @since 1.0
     */
    record ResultException( Throwable cause ) implements CommandErrorException {}
    
}
