package dev.sympho.modular_commands.api.command.parameter;

import java.io.Serializable;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.context.CommandContext;
import dev.sympho.modular_commands.api.exception.InvalidArgumentException;
import reactor.core.publisher.Mono;

// BEGIN LONG LINES
/**
 * Specification for a parameter received for a command.
 * 
 * <p>Irrespective of whether the command it is used with is compatible with interactions
 * or not, all values must be compatible with the
 * <a href="https://discord.com/developers/docs/interactions/application-commands#application-command-object">
 * Discord API specification</a> for command parameters.
 *
 * @param <T> The type of argument that is provided.
 * @param <R> The type of raw argument that is received.
 * @version 1.0
 * @since 1.0
 */
// END LONG LINES
public sealed interface Parameter<T extends @NonNull Object, R extends @NonNull Object>
        extends Serializable permits InputParameter, AttachmentParameter {

    /**
     * The name of the parameter.
     *
     * @return The name.
     */
    @Pure
    String name();

    /**
     * The description of the parameter.
     *
     * @return The description.
     * @apiNote The description must have between 1 and 100 characters.
     */
    @Pure
    String description();

    /**
     * Whether the parameter must be provided to invoke the command.
     *
     * @return {@code true} if the parameter is required, {@code false} otherwise.
     */
    @Pure
    boolean required();

    /**
     * The default value for the parameter.
     *
     * @return The default value, or {@code null} if no default.
     * @apiNote If the parameter is {@link #required() required}, then this
     *          value has no effect.
     */
    @SideEffectFree
    @Nullable T defaultValue();

    /**
     * Parses the given raw argument from the user into the corresponding value.
     *
     * @param context The execution context.
     * @param raw The raw argument received from the user.
     * @return A Mono that issues the parsed argument. If the raw value is invalid, it may
     *         fail with a {@link InvalidArgumentException}.
     * @throws InvalidArgumentException if the given string is not a valid value.
     */
    @SideEffectFree
    Mono<T> parse( CommandContext context, R raw ) throws InvalidArgumentException;

    /**
     * Constructs a new exception indicating that an argument given for this parameter
     * is invalid.
     *
     * @param message A message detailing why the argument is invalid.
     * @return The exception.
     * @apiNote This is a convenience method to use while parsing.
     */
    @SideEffectFree
    default InvalidArgumentException invalid( final String message ) {
        return new InvalidArgumentException( this, message );
    }

    /**
     * Constructs a new exception indicating that an argument given for this parameter
     * is invalid.
     *
     * @param message A message detailing why the argument is invalid.
     * @param cause The exception that caused the value to be invalid.
     * @return The exception.
     * @apiNote This is a convenience method to use while parsing.
     */
    @SideEffectFree
    default InvalidArgumentException invalid( final String message, final Throwable cause ) {
        return new InvalidArgumentException( this, message, cause );
    }
    
}
