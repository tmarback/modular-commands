package dev.sympho.modular_commands.api.command.result;

import dev.sympho.modular_commands.api.command.parameter.Parameter;

/**
 * Failure result due to a required argument not being provided.
 *
 * @param parameter The missing parameter.
 * @version 1.0
 * @since 1.0
 */
public record CommandFailureArgumentMissing( Parameter<?, ?> parameter ) 
        implements CommandFailureArgument {

    @Override
    public String message() {

        return String.format( "Missing required argument: %s", parameter.name() );

    }
    
}
