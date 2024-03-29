package dev.sympho.modular_commands.api.command.context;

import java.util.Objects;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;

import dev.sympho.bot_utils.event.RepliableContext;
import dev.sympho.modular_commands.api.command.Command;
import dev.sympho.modular_commands.api.command.Invocation;
import dev.sympho.modular_commands.api.command.parameter.Parameter;
import discord4j.common.util.Snowflake;
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
public interface CommandContext extends RepliableContext {

    /**
     * Retrieves the invocation that triggered the command.
     * 
     * <p>Unlike {@link #commandInvocation()}, the returned value may be 
     * different from the command's declared {@link Command#invocation()} if it 
     * was invoked using an alias (when supported).
     * 
     * <p>If the command has no aliases, or was invoked through a method that does not 
     * support aliases, the return of this method is the same as the return of 
     * {@link #commandInvocation()}.
     *
     * @return The trigger invocation.
     */
    @Pure
    Invocation invocation();

    /**
     * Retrieves the canonical invocation of the triggered command, that is, the value
     * of {@link Command#invocation()}. This is equivalent to the 
     * {@link #invocation() triggering invocation} after resolving any aliases.
     * 
     * <p>If the command has no aliases, or was invoked through a method that does not 
     * support aliases, the return of this method is the same as the return of 
     * {@link #invocation()}.
     *
     * @return The normalized trigger invocation.
     */
    @Pure
    Invocation commandInvocation();

    /**
     * Retrieves the user that called the command.
     *
     * @return The calling user.
     */
    @Pure
    default User caller() {
        return user();
    }

    /**
     * Retrieves the user that called the command as a guild
     * member as provided by the triggering event, if
     * present.
     *
     * @return The calling user as a guild member, or {@code null} 
     *         if the command was invoked in a private channel.
     */
    @Pure
    @Nullable Member callerMember();

    /**
     * Retrieves the user that called the command as a guild
     * member of the given guild.
     *
     * @param guildId The ID of the target guild.
     * @return The calling user as a guild member of the given guild.
     * @implNote The default implementation will re-use the member instance
     *           {@link #callerMember() provided by the event} if appropriate (that is, if the
     *           given guild is the same that the command was invoked from) to avoid making an
     *           API request.
     */
    @Pure
    default Mono<Member> callerMember( final Snowflake guildId ) {

        return Objects.requireNonNullElse( callerMember(), caller() )
                .asMember( guildId );

    }

    /**
     * @see #callerMember()
     */
    @Override
    default Mono<Member> member() {
        return Mono.justOrEmpty( callerMember() );
    }

    /**
     * @see #callerMember(Snowflake)
     */
    @Override
    default Mono<Member> member( final Snowflake guildId ) {
        return callerMember( guildId );
    }

    @Override
    Mono<MessageChannel> channel();

    /**
     * Retrieves one of the arguments to the command.
     *
     * @param <T> The type of the argument.
     * @param name The name of the corresponding parameter.
     * @param argumentType The type of the argument.
     * @return The argument value, or {@code null} if the received argument is empty
     *         (omitted by the caller or an empty parsing result) and does not have 
     *         a default value.
     * @throws IllegalArgumentException if there is no parameter with the given name.
     * @throws ClassCastException if the given argument type does not match the type of the
     *                            argument with the given name.
     * @see #getArgument(Parameter, Class)
     * @apiNote This method will never return {@code null} if the parameter provides a default 
     *          value. If it is marked as required, it will return {@code null} if and only if
     *          the parser result was empty (which implies that it will never return {@code null} 
     *          if the parser is guaranteed to never give an empty result).
     */
    @Pure
    <T extends @NonNull Object> @Nullable T getArgument( String name, Class<T> argumentType )
            throws IllegalArgumentException, ClassCastException;

    /**
     * Retrieves one of the arguments to the command.
     *
     * @param <T> The type of the argument.
     * @param parameter The corresponding parameter.
     * @param argumentType The type of the argument.
     * @return The argument value, or {@code null} if the received argument is empty
     *         (omitted by the caller or an empty parsing result) and does not have 
     *         a default value.
     * @throws IllegalArgumentException if there is no parameter with a matching name.
     * @throws ClassCastException if the given argument type does not match the type of the
     *                            argument with the given name.
     * @see #getArgument(String, Class)
     * @apiNote This is functionally equivalent to {@link #getArgument(String, Class)},
     *          but allows access directly from the parameter instance and provides
     *          compile-time type checking.
     */
    @Pure
    default <T extends @NonNull Object> @Nullable T getArgument( 
            Parameter<? extends T> parameter, Class<T> argumentType )
            throws IllegalArgumentException, ClassCastException {

        return getArgument( parameter.name(), argumentType );

    }

    /**
     * Retrieves one of the arguments to the command.
     *
     * @param <T> The type of the argument.
     * @param parameter The corresponding parameter.
     * @return The argument value, or {@code null} if the received argument is empty
     *         (omitted by the caller or an empty parsing result) and does not have 
     *         a default value.
     * @throws IllegalArgumentException if the given parameter is not present in the
     *                                  invoked command.
     * @apiNote This is functionally equivalent to {@link #getArgument(Parameter, Class)}.
     *          However, it has a stronger requirement on the {@code parameter} argument
     *          in that it must be the <i>same instance</i> (i.e., according to {@code ==}) 
     *          that was used to define the parameter in the original command, instead of
     *          just needing to match the name. This is necessary as the lack of the class
     *          parameter means there is no other way to ensure type safety. On the other
     *          hand, this variant makes it possible to use arguments that have their own
     *          type parameters in a type-safe manner.
     */
    @Pure
    <T extends @NonNull Object> @Nullable T getArgument( Parameter<? extends T> parameter )
            throws IllegalArgumentException;

    /**
     * Retrieves one of the arguments to the command expecting that it is non-null,
     * i.e. that it either has a default value or is never empty (is required and 
     * the parser never has an empty result).
     *
     * @param <T> The type of the argument.
     * @param name The name of the corresponding parameter.
     * @param argumentType The type of the argument.
     * @return The argument value.
     * @throws IllegalArgumentException if there is no parameter with the given name.
     * @throws ClassCastException if the given argument type does not match the type of the
     *                            argument with the given name.
     * @throws NullPointerException if the argument was empty (not received or empty parsing 
     *                              result) and does not have a default value.
     * @see #requireArgument(Parameter, Class)
     * @apiNote An NPE thrown by this method indicates either a mismatched configuration 
     *          (code expects the parameter to be required or default but it was not
     *          configured as such) or that the parser function unexpectedly returned
     *          an empty result.
     */
    @Pure
    default <T extends @NonNull Object> T requireArgument( String name, Class<T> argumentType )
            throws IllegalArgumentException, ClassCastException, NullPointerException {

        return Objects.requireNonNull( getArgument( name, argumentType ) );

    }

    /**
     * Retrieves one of the arguments to the command expecting that it is non-null,
     * i.e. that it either has a default value or is never empty (is required and 
     * the parser never has an empty result).
     *
     * @param <T> The type of the argument.
     * @param parameter The name of the corresponding parameter.
     * @param argumentType The type of the argument.
     * @return The argument value.
     * @throws IllegalArgumentException if there is no parameter with the given name.
     * @throws ClassCastException if the given argument type does not match the type of the
     *                            argument with the given name.
     * @throws NullPointerException if the argument was empty (not received or empty parsing 
     *                              result) and does not have a default value.
     * @see #requireArgument(String, Class)
     * @apiNote This is functionally equivalent to {@link #requireArgument(String, Class)},
     *          but allows access directly from the parameter instance and provides
     *          compile-time type checking.
     */
    @Pure
    default <T extends @NonNull Object> T requireArgument( 
            Parameter<? extends T> parameter, Class<T> argumentType ) 
            throws IllegalArgumentException, ClassCastException, NullPointerException {

        return requireArgument( parameter.name(), argumentType );

    }

    /**
     * Retrieves one of the arguments to the command expecting that it is non-null,
     * i.e. that it either has a default value or is never empty (is required and 
     * the parser never has an empty result).
     *
     * @param <T> The type of the argument.
     * @param parameter The name of the corresponding parameter.
     * @return The argument value.
     * @throws IllegalArgumentException if the given parameter is not present in the
     *                                  invoked command.
     * @throws NullPointerException if the argument was not received and does not have a
     *                              default value.
     * @apiNote This is functionally equivalent to {@link #requireArgument(Parameter, Class)}.
     *          However, it has a stronger requirement on the {@code parameter} argument
     *          in that it must be the <i>same instance</i> (i.e., according to {@code ==}) 
     *          that was used to define the parameter in the original command, instead of
     *          just needing to match the name. This is necessary as the lack of the class
     *          parameter means there is no other way to ensure type safety. On the other
     *          hand, this variant makes it possible to use arguments that have their own
     *          type parameters in a type-safe manner.
     */
    @Pure
    default <T extends @NonNull Object> T requireArgument( final Parameter<? extends T> parameter )
            throws IllegalArgumentException, NullPointerException {

        return Objects.requireNonNull( getArgument( parameter ) );

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
    default <T> T requireContext( final String key, final Class<? extends T> type )
            throws IllegalArgumentException, ClassCastException, NullPointerException {

        return Objects.requireNonNull( getContext( key, type ) );

    }
    
}
