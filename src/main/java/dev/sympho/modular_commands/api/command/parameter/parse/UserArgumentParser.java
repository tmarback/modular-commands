package dev.sympho.modular_commands.api.command.parameter.parse;

import org.checkerframework.checker.nullness.qual.NonNull;

import discord4j.core.object.entity.User;

/**
 * Parses Discord users.
 *
 * @param <T> The parsed type.
 * @version 1.0
 * @since 1.0
 */
@FunctionalInterface
public non-sealed interface UserArgumentParser<T extends @NonNull Object>
        extends EntityArgumentParser<User, T> {}
