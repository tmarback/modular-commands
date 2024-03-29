package dev.sympho.modular_commands.api.command.result;

import dev.sympho.modular_commands.api.command.parameter.Parameter;

/**
 * Failure result due to a provided argument being invalid.
 *
 * @param parameter The corresponding parameter.
 * @param error The error message.
 * @version 1.0
 * @since 1.0
 */
public record CommandFailureArgumentInvalid( Parameter<?> parameter, String error )
        implements CommandFailureArgument {

    @Override
    public String message() {

        return "%s: %s".formatted( parameter.name(), error );

    }
    
}
