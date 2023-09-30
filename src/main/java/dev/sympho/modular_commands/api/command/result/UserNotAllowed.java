package dev.sympho.modular_commands.api.command.result;

import dev.sympho.bot_utils.access.Group;

/**
 * A failure result due to the user not being part of a specific group.
 *
 * @param required The group that the user must be a part of to run the command.
 * @version 1.0
 * @since 1.0
 */
public record UserNotAllowed( Group required ) implements CommandFailure {}
