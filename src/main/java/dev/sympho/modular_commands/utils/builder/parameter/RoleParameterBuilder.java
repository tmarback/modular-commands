package dev.sympho.modular_commands.utils.builder.parameter;

import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.parameter.RoleParameter;
import discord4j.core.object.entity.Role;

/**
 * Builder for a role parameter.
 *
 * @see RoleParameter
 * @version 1.0
 * @since 1.0
 */
public final class RoleParameterBuilder 
        extends MentionableParameterBuilder<Role, RoleParameter, RoleParameterBuilder> {

    /**
     * Constructs a new builder with default values.
     */
    @Pure
    public RoleParameterBuilder() {}

    /**
     * Constructs a new builder that is a copy of the given builder.
     *
     * @param base The builder to copy.
     */
    @SideEffectFree
    public RoleParameterBuilder( final RoleParameterBuilder base ) {

        super( base );

    }

    /**
     * Constructs a new builder that is initialized to make a copy of 
     * the given parameter.
     *
     * @param base The parameter to copy.
     */
    @SideEffectFree
    public RoleParameterBuilder( final RoleParameter base ) {

        super( base );

    }

    @Override
    public RoleParameter build() throws IllegalStateException {

        try {
            return new RoleParameter( 
                    buildName(),
                    buildDescription(),
                    this.required, this.defaultValue );
        } catch ( final IllegalArgumentException e ) {
            throw new IllegalStateException( "Invalid parameter configuration.", e );
        }

    }
    
}
