package dev.sympho.modular_commands.api.command.handler;

import java.util.Collections;
import java.util.List;

import org.checkerframework.dataflow.qual.Pure;

import dev.sympho.modular_commands.api.command.context.CommandContext;
import dev.sympho.modular_commands.api.command.context.InteractionCommandContext;
import dev.sympho.modular_commands.api.command.context.MessageCommandContext;
import dev.sympho.modular_commands.api.command.context.SlashCommandContext;

/**
 * A set of handlers for executing an invocation of a command.
 * 
 * <p>Note that the implemented subinterfaces of this type determine what kinds of commands are 
 * supported; if the handlers are specified through a {@link MessageHandlers}, the command will
 * only support invocations through messages, even if the invocation handler actually supports
 * slash commands. Implementing more than one is allowed, however, and some joint-type interfaces
 * are provided for convenience.
 * 
 * <p>All instances of this class, as well as any contained collections, should be unmodifiable.
 *
 * @version 1.0
 * @since 1.0
 */
public sealed interface Handlers permits MessageHandlers, SlashHandlers {

    /**
     * The handler to use for executing the invocation.
     *
     * @return The handler.
     */
    @Pure
    InvocationHandler<?> invocation();

    /**
     * The handlers to use to handle the result (in the order given).
     *
     * @return The handlers.
     */
    @Pure
    List<? extends ResultHandler<?>> result();

    /**
     * Creates a handler set with the given handlers, with support only for
     * message-based commands.
     *
     * @param invocation The invocation handler.
     * @param result The result handlers.
     * @return The handler set.
     */
    static MessageHandlers message( 
            final InvocationHandler<? super MessageCommandContext> invocation,
            List<? extends ResultHandler<? super MessageCommandContext>> result
    ) {

        return new MessageHandlers.Impl( invocation, result );

    }

    /**
     * Creates a handler set with the given handlers, with support only for
     * message-based commands.
     *
     * @param invocation The invocation handler.
     * @param result The result handlers.
     * @return The handler set.
     */
    @SafeVarargs
    @SuppressWarnings( "varargs" )
    static MessageHandlers message( 
            final InvocationHandler<? super MessageCommandContext> invocation,
            ResultHandler<? super MessageCommandContext>... result
    ) {

        return message( invocation, List.of( result ) );

    }

    /**
     * Creates a handler set with the given handlers, with support only for
     * message-based commands, and no result handlers.
     *
     * @param invocation The invocation handler.
     * @return The handler set.
     */
    static MessageHandlers message( 
            final InvocationHandler<? super MessageCommandContext> invocation 
    ) {

        return message( invocation, Collections.emptyList() );

    }

    /**
     * Creates a handler set with the given handlers, with support only for
     * slash-based commands.
     *
     * @param invocation The invocation handler.
     * @param result The result handlers.
     * @return The handler set.
     */
    static SlashHandlers slash(
            final InvocationHandler<? super SlashCommandContext> invocation,
            List<? extends ResultHandler<? super SlashCommandContext>> result
    ) {

        return new SlashHandlers.Impl( invocation, result );

    }

    /**
     * Creates a handler set with the given handlers, with support only for
     * slash-based commands.
     *
     * @param invocation The invocation handler.
     * @param result The result handlers.
     * @return The handler set.
     */
    @SafeVarargs
    @SuppressWarnings( "varargs" )
    static SlashHandlers slash(
            final InvocationHandler<? super SlashCommandContext> invocation,
            ResultHandler<? super SlashCommandContext>... result
    ) {

        return slash( invocation, List.of( result ) );

    }

    /**
     * Creates a handler set with the given handlers, with support only for
     * slash-based commands, and no result handlers.
     *
     * @param invocation The invocation handler.
     * @return The handler set.
     */
    static SlashHandlers slash(
            final InvocationHandler<? super SlashCommandContext> invocation
    ) {

        return slash( invocation, Collections.emptyList() );

    }

    /**
     * Creates a handler set with the given handlers, with support for
     * interaction-based commands.
     *
     * @param invocation The invocation handler.
     * @param result The result handlers.
     * @return The handler set.
     */
    static InteractionHandlers interaction(
            final InvocationHandler<? super InteractionCommandContext> invocation,
            List<? extends ResultHandler<? super InteractionCommandContext>> result
    ) {

        return new InteractionHandlers.Impl( invocation, result );

    }

    /**
     * Creates a handler set with the given handlers, with support for
     * interaction-based commands.
     *
     * @param invocation The invocation handler.
     * @param result The result handlers.
     * @return The handler set.
     */
    @SafeVarargs
    @SuppressWarnings( "varargs" )
    static InteractionHandlers interaction(
            final InvocationHandler<? super InteractionCommandContext> invocation,
            ResultHandler<? super InteractionCommandContext>... result
    ) {

        return interaction( invocation, List.of( result ) );

    }

    /**
     * Creates a handler set with the given handlers, with support only for
     * interaction-based commands, and no result handlers.
     *
     * @param invocation The invocation handler.
     * @return The handler set.
     */
    static InteractionHandlers interaction(
            final InvocationHandler<? super InteractionCommandContext> invocation
    ) {

        return interaction( invocation, Collections.emptyList() );

    }

    /**
     * Creates a handler set with the given handlers, with support for
     * text-based commands.
     *
     * @param invocation The invocation handler.
     * @param result The result handlers.
     * @return The handler set.
     */
    static TextHandlers text(
            final InvocationHandler<CommandContext> invocation,
            List<? extends ResultHandler<CommandContext>> result
    ) {

        return new TextHandlers.Impl( invocation, result );

    }

    /**
     * Creates a handler set with the given handlers, with support for
     * text-based commands.
     *
     * @param invocation The invocation handler.
     * @param result The result handlers.
     * @return The handler set.
     */
    @SafeVarargs
    @SuppressWarnings( "varargs" )
    static TextHandlers text(
            final InvocationHandler<CommandContext> invocation,
            ResultHandler<CommandContext>... result
    ) {

        return text( invocation, List.of( result ) );

    }

    /**
     * Creates a handler set with the given handlers, with support only for
     * text-based commands, and no result handlers.
     *
     * @param invocation The invocation handler.
     * @return The handler set.
     */
    static TextHandlers text(
            final InvocationHandler<CommandContext> invocation
    ) {

        return text( invocation, Collections.emptyList() );

    }
    
}
