package dev.sympho.modular_commands.api.command.parameter.parse;

import org.checkerframework.checker.nullness.qual.NonNull;

import discord4j.core.object.entity.Entity;

/**
 * Parses Discord entities.
 *
 * @param <E> The entity type.
 * @param <T> The parsed type.
 * @version 1.0
 * @since 1.0
 */
public sealed interface EntityArgumentParser<E extends @NonNull Entity, T extends @NonNull Object>
        extends InputParser<E, T> permits UserArgumentParser, RoleArgumentParser, 
        ChannelArgumentParser, MessageArgumentParser {}
