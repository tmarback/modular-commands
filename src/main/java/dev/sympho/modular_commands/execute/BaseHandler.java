package dev.sympho.modular_commands.execute;

import java.util.Objects;

import dev.sympho.bot_utils.access.NamedGroup;
import dev.sympho.modular_commands.api.command.context.CommandContext;
import dev.sympho.modular_commands.api.command.context.MessageCommandContext;
import dev.sympho.modular_commands.api.command.handler.ResultHandler;
import dev.sympho.modular_commands.api.command.result.CommandError;
import dev.sympho.modular_commands.api.command.result.CommandFailure;
import dev.sympho.modular_commands.api.command.result.CommandFailureMessage;
import dev.sympho.modular_commands.api.command.result.CommandResult;
import dev.sympho.modular_commands.api.command.result.CommandSuccess;
import dev.sympho.modular_commands.api.command.result.CommandSuccessAck;
import dev.sympho.modular_commands.api.command.result.CommandSuccessMessage;
import dev.sympho.modular_commands.api.command.result.UserNotAllowed;
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
    public static final ResultHandler<CommandContext> DEFAULT = BaseHandler::defaultHandler;

    /** The current base handler. */
    private static ResultHandler<CommandContext> base = DEFAULT;

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
    public static ResultHandler<CommandContext> get() {
        return base;
    }

    /** 
     * Sets the base handler.
     *
     * @param handler The new base handler.
     */
    public static void set( final ResultHandler<CommandContext> handler ) {
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
            return Mono.empty(); // Not recognized
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

        final String message;
        if ( result instanceof CommandSuccessMessage res ) {
            message = res.message();
            final var embed = EmbedCreateSpec.builder()
                    .title( "Success" )
                    .color( COLOR_SUCCESS )
                    .description( message )
                    .build();

            return context.reply( embed ).thenReturn( true );
        } else if ( result instanceof CommandSuccessAck res ) {
            if ( context instanceof MessageCommandContext ctx ) {
                return ctx.getMessage()
                        .addReaction( res.react() )
                        .thenReturn( true );
            } else {
                return context.replies().add()
                        .withContent( res.message() )
                        .withPrivately( true )
                        .thenReturn( true );
            }
        } else {
            return Mono.just( true );
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

        final String message;
        if ( result instanceof UserNotAllowed res ) {
            final var required = res.required();
            if ( required instanceof NamedGroup req ) {
                message = "Only users in the %s group can use this command."
                        .formatted( req.name() );
            } else {
                message = "You cannot use this command.";
            }
        } else if ( result instanceof CommandFailureMessage res ) {
            message = res.message();
        } else {
            return Mono.just( true );
        }

        final var embed = EmbedCreateSpec.builder()
                .title( "Error" )
                .color( COLOR_FAILURE )
                .description( message )
                .build();
        return context.reply( embed ).thenReturn( true );

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

        return context.reply( embed ).thenReturn( true );

    }
    
}
