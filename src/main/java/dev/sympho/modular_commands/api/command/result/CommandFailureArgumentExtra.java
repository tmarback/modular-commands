package dev.sympho.modular_commands.api.command.result;

import java.util.List;

/**
 * Failure result due to one or more argument being provided where one wasn't expected.
 *
 * @param args The unexpected arguments.
 * @version 1.0
 * @since 1.0
 */
public record CommandFailureArgumentExtra( List<String> args ) implements CommandFailureArgument {

    @Override
    public String message() {

        return String.format( "Unexpected arguments: %s", args );

    }
    
}
