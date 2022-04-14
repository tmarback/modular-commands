package dev.sympho.modular_commands.utils.builder.parameter;

import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.parameter.MentionParameter;
import discord4j.core.object.entity.Entity;

/**
 * Base for a mention parameter builder.
 *
 * @param <T> The type of entity received by the parameter.
 * @param <P> The parameter type.
 * @param <SELF> The self type.
 * @see MentionParameter
 * @version 1.0
 * @since 1.0
 */
abstract sealed class MentionParameterBuilder<
            T extends Entity, 
            P extends MentionParameter<T>,
            SELF extends EntityParameterBuilder<T, P, SELF>
        > extends EntityParameterBuilder<T, P, SELF> permits ChannelParameterBuilder {

    /**
     * Constructs a new builder with default values.
     */
    @Pure
    protected MentionParameterBuilder() {}

    /**
     * Constructs a new builder that is a copy of the given builder.
     *
     * @param base The builder to copy.
     */
    @SideEffectFree
    protected MentionParameterBuilder(
            final MentionParameterBuilder<? extends T, ?, ?> base ) {

        super( base );

    }

    /**
     * Constructs a new builder that is initialized to make a copy of 
     * the given parameter.
     *
     * @param base The parameter to copy.
     */
    @SideEffectFree
    protected MentionParameterBuilder( final MentionParameter<T> base ) {

        super( base );

    }
    
}
