package dev.sympho.modular_commands.api.command.parameter.parse;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.common.value.qual.IntRange;
import org.checkerframework.dataflow.qual.Pure;

/**
 * Parses string-based input arguments.
 *
 * @param <T> The type of argument that is provided.
 * @version 1.0
 * @since 1.0
 * @apiNote Implementations do not have to verify in parsing that the length of the received raw 
 *          value is within the range specified by {@link #minLength()} and {@link #maxLength()}.
 *          The system will automatically do this verification before invoking the parser.
 */
@FunctionalInterface
public non-sealed interface StringParser<T extends @NonNull Object> 
        extends ChoicesParser<String, T> {

    /** The maximum length possible (in the Discord API for application commands). */
    int MAX_LENGTH = 6000;

    /**
     * The minimum length allowed (inclusive).
     *
     * @return The length, or {@code null} if no minimum.
     * @implSpec The default is {@code null}. Must be between 0 and {@value #MAX_LENGTH}.
     */
    @Pure
    @IntRange( from = 0, to = MAX_LENGTH )
    default @Nullable Integer minLength() {
        return null;
    }

    /**
     * The maximum length allowed (inclusive).
     *
     * @return The length, or {@code null} if no maximum.
     * @implSpec The default is {@code null}. Must be between 1 and {@value #MAX_LENGTH}.
     */
    @Pure
    @IntRange( from = 1, to = MAX_LENGTH )
    default @Nullable Integer maxLength() {
        return null;
    }

    /**
     * Whether to allow merge behavior when a parameter using this parser is the last parameter 
     * of the command.
     * 
     * <p>If enabled, whenever an associated parameter is the last parameter of a message-based 
     * invocation, all content in the message beyond the second-to-last argument is considered 
     * to correspond to this parameter, and is parsed as-is, rather than being tokenized like 
     * the preceding arguments.
     * 
     * <p>This behavior allows the last argument to contain delimiter characters (such as spaces)
     * and generally maintain formatting without the user needing to surround it with quotes
     * (particularly useful when the last parameter is a message or a list). 
     * 
     * <p>One consequence of it, however, is that if the user <i>does</i> include quotes
     * (out of habit, for example), they will not be stripped prior to parsing. It also
     * disables automatic checking for extra arguments, which may cause parsing errors instead.
     * 
     * <p>If this parameter is not the last parameter, or the invocation is not through a
     * message, this value is ignored.
     *
     * @return Whether to enable merge behavior.
     * @implSpec The default is {@code false}.
     */
    @Pure
    default boolean allowMerge() {
        return false;
    }

    /**
     * Verifies that the given string is within the allowed length range for this parser.
     *
     * @param raw The string to validate.
     * @return The given string.
     * @throws InvalidArgumentException If the length is outside the allowed range.
     * @apiNote Implementations do not need to call this method.
     */
    @Pure
    default String verifyLength( final String raw ) throws InvalidArgumentException {

        final var min = minLength();
        final var max = maxLength();
        final var length = raw.length();

        if ( min != null && length < min ) {
            throw new InvalidArgumentException( 
                    "Must have at least %d characters".formatted( min ) );
        }

        if ( max != null && length > max ) {
            throw new InvalidArgumentException( 
                    "Must have at most %d characters".formatted( max ) );
        }

        return raw;

    }

    @Override
    default String validateRaw( final String raw ) throws InvalidArgumentException {
        return verifyLength( ChoicesParser.super.validateRaw( raw ) );
    }

}
