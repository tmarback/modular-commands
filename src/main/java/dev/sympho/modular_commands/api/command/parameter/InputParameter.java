package dev.sympho.modular_commands.api.command.parameter;

import org.checkerframework.checker.nullness.qual.NonNull;

import dev.sympho.modular_commands.api.command.parameter.parse.InputParser;

/**
 * Specification for a parameter received as part of the command.
 *
 * @param <T> The type of parameter that is received.
 * @version 1.0
 * @since 1.0
 */
public sealed interface InputParameter<T extends @NonNull Object> 
        extends Parameter<T, String>, InputParser<T>
        permits ChoicesParameter, EntityParameter {}
