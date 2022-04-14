package dev.sympho.modular_commands.api.command.context;

import java.util.Objects;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;

import dev.sympho.modular_commands.api.command.Command;
import dev.sympho.modular_commands.api.command.Invocation;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.Event;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import reactor.core.publisher.Mono;

/**
 * The execution context of an invoked command.
 *
 * @version 1.0
 * @since 1.0
 */
public sealed interface CommandContext permits LazyContext, 
        MessageCommandContext, InteractionCommandContext {

    /**
     * Retrieves the event that triggered the command.
     *
     * @return The trigger event.
     */
    @Pure
    Event getEvent();

    /**
     * Retrieves the client where the command was received.
     *
     * @return The client.
     */
    @Pure
    default GatewayDiscordClient getClient() {

        return getEvent().getClient();

    }

    /**
     * Retrieves the invocation that triggered the command.
     * 
     * <p>This may be different from the command's declared {@link Command#invocation()}
     * if it was invoked using an alias (when supported).
     *
     * @return The trigger invocation.
     */
    @Pure
    Invocation getInvocation();

    /**
     * Retrieves the user that called the command.
     *
     * @return The calling user.
     */
    @Pure
    User getCaller();

    /**
     * Retries the user that called the command as a guild
     * member as provided by the triggering event, if
     * present.
     *
     * @return The calling user as a guild member, or 
     */
    @Pure
    @Nullable Member getCallerMember();

    /**
     * Retrieves the channel that the command was invoked in.
     *
     * @return The invoking channel.
     */
    Mono<MessageChannel> getChannel();

    /**
     * Retrieves the ID of the channel that the command was invoked in.
     *
     * @return The invoking channel's ID.
     */
    @Pure
    Snowflake getChannelId();

    /**
     * Retrieves the guild that the command was invoked in, if there
     * is one.
     *
     * @return The invoking guild.
     */
    Mono<Guild> getGuild();

    /**
     * Retrieves the ID of the guild that the command was invoked in, if there
     * is one.
     *
     * @return The invoking guild's ID, or {@code null} if the command was
     *         invoked in a private channel.
     */
    @Pure
    @Nullable Snowflake getGuildId();

    /**
     * Retrieves one of the arguments to the command.
     *
     * @param <T> The type of the argument.
     * @param name The name of the corresponding parameter.
     * @param argumentType The type of the argument.
     * @return The argument value, or {@code null} if the argument was not given by
     *         the caller and does not have a default value.
     * @throws IllegalArgumentException if there is no parameter with the given name.
     * @throws ClassCastException if the given argument type does not match the type of the
     *                            argument with the given name.
     * @apiNote This method will never return {@code null} if the parameter is marked
     *          as required or provides a default value.
     */
    @Pure
    <T> @Nullable T getArgument( String name, Class<? extends T> argumentType )
            throws IllegalArgumentException, ClassCastException;

    /**
     * Retrieves one of the arguments to the command, assuming it is non-null
     * (so either required or with a default value).
     *
     * @param <T> The type of the argument.
     * @param name The name of the corresponding parameter.
     * @param argumentType The type of the argument.
     * @return The argument value, or {@code null} if the argument was not given by
     *         the caller and does not have a default value.
     * @throws IllegalArgumentException if there is no parameter with the given name.
     * @throws ClassCastException if the given argument type does not match the type of the
     *                            argument with the given name.
     * @throws NullPointerException if the argument was not received and does not have a
     *                              default value.
     * @apiNote An NPE thrown by this method indicates a mismatched configuration 
     *          (code expects the parameter to be required or default but it was not
     *          configured as such).
     */
    @Pure
    default <T> T requireArgument( String name, Class<? extends T> argumentType )
            throws IllegalArgumentException, ClassCastException, NullPointerException {

        return Objects.requireNonNull( getArgument( name, argumentType ) );

    }

    /**
     * Places a context object for subsequent handlers, optionally replacing any existing
     * values under the same key.
     *
     * @param key The object key.
     * @param obj The object to store.
     * @param replace If {@code true}, the object will be placed unconditionally, replacing
     *                any existing value in that key. Otherwise, it will only be placed if there
     *                are no values with the given key.
     * @return {@code true} if the given object was placed in the context. If {@code replace}
     *         is {@code false} and there is already an object at the given key, returns
     *         {@code false}.
     * @apiNote This method is <i>not</i> thread-safe.
     */
    boolean setContext( String key, @Nullable Object obj, boolean replace );

    /**
     * Unconditionally places a context object for subsequent handlers.
     *
     * @param key The object key.
     * @param obj The object to store.
     * @see #setContext(String, Object, boolean)
     * @apiNote This method is equivalent to 
     *          {@link #setContext(String, Object, boolean) setContext(key, obj, true)}.
     */
    default void setContext( String key, @Nullable Object obj ) {
        setContext( key, obj, true );
    }

    /**
     * Retrieves a context object set by {@link #setContext(String, Object, boolean)}.
     *
     * @param <T> The type of the object.
     * @param key The object key.
     * @param type The object class.
     * @return The context object.
     * @throws IllegalArgumentException if there is no context object with the given key.
     * @throws ClassCastException if the context object with the given key is not compatible
     *                            with the given type (not the same or a subtype).
     */
    @Pure
    <T> @Nullable T getContext( String key, Class<? extends T> type )
            throws IllegalArgumentException, ClassCastException;

    /**
     * Retrieves a non-null context object set by {@link #setContext(String, Object, boolean)}.
     *
     * @param <T> The type of the object.
     * @param key The object key.
     * @param type The object class.
     * @return The context object.
     * @throws IllegalArgumentException If there is no context object with the given key.
     * @throws ClassCastException if the context object with the given key is not compatible
     *                            with the given type (not the same or a subtype).
     * @throws NullPointerException if the context object was {@code null}.
     */
    @Pure
    default <T> T requireContext( String key, Class<? extends T> type )
            throws IllegalArgumentException, ClassCastException, NullPointerException {

        return Objects.requireNonNull( getContext( key, type ) );

    }
    
}
