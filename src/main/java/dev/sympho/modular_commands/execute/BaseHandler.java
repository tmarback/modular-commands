package dev.sympho.modular_commands.execute;

import java.util.Objects;

import dev.sympho.modular_commands.api.command.context.CommandContext;
import dev.sympho.modular_commands.api.command.handler.AnyResultHandler;
import dev.sympho.modular_commands.api.command.result.CommandError;
import dev.sympho.modular_commands.api.command.result.CommandFailure;
import dev.sympho.modular_commands.api.command.result.CommandFailureMessage;
import dev.sympho.modular_commands.api.command.result.CommandResult;
import dev.sympho.modular_commands.api.command.result.CommandSuccess;
import dev.sympho.modular_commands.api.command.result.CommandSuccessMessage;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

/**
 * Provides the global base result handler. This handler is used after a command is
 * executed if none of the context-specific handlers completed handling of that
 * result. In other words, it is a fallback/default handler.
 *
 * @version 1.0
 * @since 1.0
 */
public final class BaseHandler {

    /** The initial/default base handler. */
    public static final AnyResultHandler DEFAULT = BaseHandler::defaultHandler;

    /** The current base handler. */
    private static AnyResultHandler base = DEFAULT;

    /** The color to use for success embeds. */
    private static final Color COLOR_SUCCESS = Color.GREEN;

    /** The color to use for failure embeds. */
    private static final Color COLOR_FAILURE = Color.RED;

    /** The color to use for error embeds. */
    private static final Color COLOR_ERROR = Color.GRAY;

    /** Do not instantiate. */
    private BaseHandler() {}

    /** 
     * Retrieves the current base handler. 
     *
     * @return The base handler.
     */
    public static AnyResultHandler get() {
        return base;
    }

    /** 
     * Sets the base handler.
     *
     * @param handler The new base handler.
     */
    public static void set( final AnyResultHandler handler ) {
        base = Objects.requireNonNull( handler );
    }

    /**
     * The default result handler.
     *
     * @param context The execution context.
     * @param result The execution result.
     * @return If fully handled.
     */
    private static Mono<Boolean> defaultHandler( final CommandContext context, 
            final CommandResult result ) {

        if ( result instanceof CommandSuccess success ) {
            return handleSuccess( context, success );
        } else if ( result instanceof CommandFailure failure ) {
            return handleFailure( context, failure );
        } else if ( result instanceof CommandError error ) {
            return handleError( context, error );
        } else {
            return Mono.just( true ); // Not recognized
        }

    }

    /**
     * Handles a success result.
     *
     * @param context The execution context.
     * @param result The execution result.
     * @return If fully handled.
     */
    private static Mono<Boolean> handleSuccess( final CommandContext context, 
            final CommandSuccess result ) {

        if ( result instanceof CommandSuccessMessage res ) {
            final var message = res.message();
            final var embed = EmbedCreateSpec.builder()
                    .title( "Success" )
                    .color( COLOR_SUCCESS )
                    .description( message )
                    .build();

            // TODO: Change to reply manager
            return context.getChannel()
                    .flatMap( ch -> ch.createMessage( embed ) )
                    .thenReturn( false );
        } else {
            return Mono.just( false );
        }

    }

    /**
     * Handles a failure result.
     *
     * @param context The execution context.
     * @param result The execution result.
     * @return If fully handled.
     */
    private static Mono<Boolean> handleFailure( final CommandContext context, 
            final CommandFailure result ) {

        if ( result instanceof CommandFailureMessage res ) {
            final var message = res.message();
            final var embed = EmbedCreateSpec.builder()
                    .title( "Error" )
                    .color( COLOR_FAILURE )
                    .description( message )
                    .build();

            // TODO: Change to reply manager
            return context.getChannel()
                    .flatMap( ch -> ch.createMessage( embed ) )
                    .thenReturn( false );
        } else {
            return Mono.just( false );
        }

    }

    /**
     * Handles an error result.
     *
     * @param context The execution context.
     * @param result The execution result.
     * @return If fully handled.
     */
    private static Mono<Boolean> handleError( final CommandContext context, 
            final CommandError result ) {

        final var message = result.message();
        final var embed = EmbedCreateSpec.builder()
                .title( "Internal Error" )
                .color( COLOR_ERROR )
                .description( message )
                .build();

        // TODO: Change to reply manager
        return context.getChannel()
                .flatMap( ch -> ch.createMessage( embed ) )
                .thenReturn( false );

    }
    
}
