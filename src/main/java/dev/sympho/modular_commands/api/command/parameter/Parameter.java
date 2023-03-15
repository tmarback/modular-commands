package dev.sympho.modular_commands.api.command.parameter;

import javax.annotation.Nullable;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.regex.qual.Regex;
import org.checkerframework.common.value.qual.MatchesRegex;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;
import org.immutables.value.Value;

import dev.sympho.modular_commands.api.command.Command;
import dev.sympho.modular_commands.api.command.parameter.parse.ArgumentParser;
import dev.sympho.modular_commands.utils.ParameterUtils;

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
 * @version 1.0
 * @since 1.0
 */
// END LONG LINES
@Value.Immutable
@Value.Style( 
        visibility = Value.Style.ImplementationVisibility.PACKAGE, 
        overshadowImplementation = true
)
public interface Parameter<T extends @NonNull Object> {

    /** Pattern for valid parameter names in the Discord API. */
    @Regex String NAME_REGEX = Command.NAME_REGEX;
    /** Pattern for valid parameter descriptions in the Discord API. */
    @Regex String DESCRIPTION_REGEX = Command.DESCRIPTION_REGEX;

    /**
     * The name of the parameter.
     *
     * @return The value.
     */
    @Pure
    @MatchesRegex( NAME_REGEX ) String name();

    /**
     * The description of the parameter.
     *
     * @return The value.
     */
    @Pure
    @MatchesRegex( DESCRIPTION_REGEX ) String description();

    /**
     * Whether the parameter must be provided to invoke the command.
     *
     * @return The value.
     * @implSpec The default is {@code false}.
     */
    @Pure
    @Value.Default
    default boolean required() {
        return false;
    }

    /**
     * The default value for the parameter.
     * 
     * <p>If {@code null}, the parameter has no default and will be {@code null} if missing.
     * 
     * <p>Note that this property is only meaningful if {@link #required()} is {@code false}.
     *
     * @return The value.
     * @implSpec The default is {@code null}.
     */
    @Pure
    // TODO: Replace JSR305 @Nullable with Checker's 
    // Blocked by https://github.com/immutables/immutables/issues/1262
    @Nullable T defaultValue();

    /**
     * The parser to use to process received arguments.
     *
     * @return The value.
     */
    @Pure
    ArgumentParser<?, T> parser();

    /**
     * Validates that the properties of this instance are valid.
     *
     * @throws IllegalArgumentException if any of properties are invalid.
     * @throws NullPointerException if a {@code null} value was found where not allowed.
     * @see ParameterUtils#validate(Parameter)
     */
    @Pure
    @Value.Check
    default void validate() throws IllegalArgumentException, NullPointerException {

        ParameterUtils.validate( this );

    }

    /**
     * Creates a new builder.
     *
     * @param <T> The argument type.
     * @return The builder.
     */
    @SideEffectFree
    static <T extends @NonNull Object> Builder<T> builder() {
        return new Builder<>();
    }

    /**
     * Creates a new builder initialized with the properties of the given parameter.
     *
     * @param <T> The argument type.
     * @param base The base instance to copy.
     * @return The builder.
     */
    @SideEffectFree
    static <T extends @NonNull Object> Builder<T> builder( final Parameter<T> base ) {
        return new Builder<T>().from( base );
    }

    /**
     * The default builder.
     *
     * @param <T> The argument type.
     * @since 1.0
     */
    @SuppressWarnings( "MissingCtor" )
    class Builder<T extends @NonNull Object> extends ImmutableParameter.Builder<T> {}

}
