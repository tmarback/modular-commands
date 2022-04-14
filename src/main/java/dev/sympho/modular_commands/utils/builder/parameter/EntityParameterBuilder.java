package dev.sympho.modular_commands.utils.builder.parameter;

import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.parameter.EntityParameter;
import discord4j.core.object.entity.Entity;

/**
 * Base for an entity parameter builder.
 *
 * @param <T> The type of entity received by the parameter.
 * @param <P> The parameter type.
 * @param <SELF> The self type.
 * @see EntityParameter
 * @version 1.0
 * @since 1.0
 */
abstract sealed class EntityParameterBuilder<
            T extends Entity, 
            P extends EntityParameter<T>,
            SELF extends EntityParameterBuilder<T, P, SELF>
        > extends ParameterBuilder<T, P, SELF> 
        permits MentionableParameterBuilder, MessageParameterBuilder {

    /**
     * Constructs a new builder with default values.
     */
    @Pure
    protected EntityParameterBuilder() {}

    /**
     * Constructs a new builder that is a copy of the given builder.
     *
     * @param base The builder to copy.
     */
    @SideEffectFree
    protected EntityParameterBuilder( final EntityParameterBuilder<? extends T, ?, ?> base ) {

        super( base );

    }

    /**
     * Constructs a new builder that is initialized to make a copy of 
     * the given parameter.
     *
     * @param base The parameter to copy.
     */
    @SideEffectFree
    protected EntityParameterBuilder( final EntityParameter<T> base ) {

        super( base );

    }
    
}
