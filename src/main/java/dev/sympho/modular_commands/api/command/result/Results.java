package dev.sympho.modular_commands.api.command.result;

import discord4j.core.object.reaction.ReactionEmoji;
import reactor.core.publisher.Mono;

/**
 * Utilities for generating results from handlers.
 *
 * @version 1.0
 * @since 1.0
 */
public final class Results {

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
     * Generates a result indicating the command executed successfully with no further context.
     *
     * @return The generated result.
     */
    public static CommandSuccess ok() {

        return OK;

    }

    /**
     * Alias for {@link #ok()} that casts to a 
     * plain Result to avoid Generics issues.
     *
     * @return The result.
     */
    public static CommandResult okR() {
        return ok();
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
     * Alias for {@link #fail()} that casts to a 
     * plain Result to avoid Generics issues.
     *
     * @return The result.
     */
    public static CommandResult failR() {
        return fail();
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
     * Alias for {@link #success(String)} that casts to a 
     * plain Result to avoid Generics issues.
     *
     * @param message The message to the user.
     * @return The result.
     */
    public static CommandResult successR( final String message ) {
        return success( message );
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
     * Alias for {@link #failure(String)} that casts to a 
     * plain Result to avoid Generics issues.
     *
     * @param message The message to the user.
     * @return The result.
     */
    public static CommandResult failureR( final String message ) {
        return failure( message );
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
     * Alias for {@link #error(String)} that casts to a 
     * plain Result to avoid Generics issues.
     *
     * @param message The message to the user.
     * @return The result.
     */
    public static CommandResult errorR( final String message ) {
        return error( message );
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
     * Alias for {@link #exception(Throwable)} that casts to a 
     * plain Result to avoid Generics issues.
     *
     * @param cause The exception that caused the error.
     * @return The result.
     */
    public static CommandResult exceptionR( final Throwable cause ) {
        return exception( cause );
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

    /**
     * Generates a result that acknowledges to the user that the command finished executing.
     *
     * @param react The react to use if the command was triggered by a message.
     * @param message The message to send if the command was not triggered by a message.
     * @return The result.
     */
    public static CommandSuccessAck ack( final ReactionEmoji react, final String message ) {

        return new ResultSuccessAck( react, message );
        
    }

    /**
     * Alias for {@link #ack(ReactionEmoji, String)} that casts to a 
     * plain Result to avoid Generics issues.
     *
     * @param react The react to use if the command was triggered by a message.
     * @param message The message to send if the command was not triggered by a message.
     * @return The result.
     */
    public static CommandResult ackR( final ReactionEmoji react, final String message ) {

        return ack( react, message );
        
    }

    /**
     * Generates a result that acknowledges to the user that the command finished executing.
     *
     * @param react The react to use if the command was triggered by a message.
     * @param message The message to send if the command was not triggered by a message.
     * @return A Mono that issues the generated result.
     * @see #ack(ReactionEmoji, String)
     */
    public static Mono<CommandResult> ackMono( final ReactionEmoji react, final String message ) {

        return Mono.just( ack( react, message ) );
        
    }

    /* Implementations for the returns */

    /** 
     * Implementation for {@link Results#ok()}. 
     *
     * @since 1.0
     */
    record ResultOK() implements CommandSuccess {}

    /** 
     * Implementation for {@link Results#fail()}. 
     *
     * @since 1.0
     */
    record ResultFail() implements CommandFailure {}

    /**
     * Implementation for {@link Results#success(String)}.
     *
     * @param message The message to the user.
     * @since 1.0
     */
    record ResultSuccessMessage( String message ) implements CommandSuccessMessage {}

    /**
     * Implementation for {@link Results#failure(String)}.
     *
     * @param message The message to the user.
     * @since 1.0
     */
    record ResultFailureMessage( String message ) implements CommandFailureMessage {}

    /**
     * Implementation for {@link Results#error(String)}.
     *
     * @param message The error message.
     * @since 1.0
     */
    record ResultError( String message ) implements CommandError {}

    /**
     * Implementation for {@link Results#exception(Throwable)}.
     *
     * @param cause The exception that caused the error.
     * @since 1.0
     */
    record ResultException( Throwable cause ) implements CommandErrorException {}

    /**
     * Implementation for {@link Results#ack(ReactionEmoji, String)}.
     *
     * @param react The react to use if the command was triggered by a message.
     * @param message The message to send if the command was not triggered by a message.
     * @since 1.0
     */
    record ResultSuccessAck( ReactionEmoji react, String message ) implements CommandSuccessAck {}
    
}
