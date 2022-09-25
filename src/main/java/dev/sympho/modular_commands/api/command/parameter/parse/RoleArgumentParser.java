package dev.sympho.modular_commands.api.command.parameter.parse;

import org.checkerframework.checker.nullness.qual.NonNull;

import discord4j.core.object.entity.Role;

/**
 * Parses Discord roles.
 *
 * @param <T> The parsed type.
 * @version 1.0
 * @since 1.0
 */
@FunctionalInterface
public non-sealed interface RoleArgumentParser<T extends @NonNull Object>
        extends EntityArgumentParser<Role, T> {}
