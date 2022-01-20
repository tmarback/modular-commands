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
    public static Mono<CommandContinue> cont() {

        return Mono.just( CONTINUE );

    }

    /**
     * Generates a result indicating the command executed successfully with no further context.
     *
     * @return The generated result.
     */
    public static Mono<CommandSuccess> ok() {

        return Mono.just( OK );

    }

    /**
     * Generates a result indicating the command failed with no further context.
     *
     * @return The generated result.
     */
    public static Mono<CommandFailure> fail() {

        return Mono.just( FAIL );

    }

    /**
     * Generates a result indicating the command executed successfully with a message
     * to the user.
     *
     * @param message The message to the user.
     * @return The generated result.
     */
    public static Mono<CommandSuccessMessage> success( final String message ) {

        return Mono.just( new ResultSuccessMessage( message ) );

    }

    /**
     * Generates a result indicating the command failed with a message to the user.
     *
     * @param message The message to the user.
     * @return The generated result.
     */
    public static Mono<CommandFailureMessage> failure( final String message ) {

        return Mono.just( new ResultFailureMessage( message ) );

    }

    /**
     * Generates a result indicating the command encountered an error.
     *
     * @param message The error message.
     * @return The generated result.
     */
    public static Mono<CommandError> error( final String message ) {

        return Mono.just( new ResultError( message ) );

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
    public static Mono<CommandErrorException> exception( final Throwable cause ) {

        return Mono.just( new ResultException( cause ) );

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
