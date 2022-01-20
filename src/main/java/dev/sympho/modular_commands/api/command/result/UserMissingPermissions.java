package dev.sympho.modular_commands.api.command.result;

import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;

/**
 * A failure result due to the user having insufficient permissions.
 *
 * @param missingDiscordPermissions The built-in permissions that the user is missing but are
 *                                  required to run the command.
 * @version 1.0
 * @since 1.0
 */
public record UserMissingPermissions( PermissionSet missingDiscordPermissions ) 
        implements CommandFailureMessage {

    @Override
    public String message() {

        final Iterable<String> missing = () -> missingDiscordPermissions().stream()
                .map( Permission::toString ).iterator();
        return String.format( "Missing permissions: %s", String.join( ", ", missing ) );

    }
    
}
