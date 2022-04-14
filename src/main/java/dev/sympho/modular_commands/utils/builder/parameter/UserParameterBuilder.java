package dev.sympho.modular_commands.utils.builder.parameter;

import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.parameter.UserParameter;
import discord4j.core.object.entity.User;

/**
 * Builder for a user parameter.
 *
 * @see UserParameter
 * @version 1.0
 * @since 1.0
 */
public final class UserParameterBuilder 
        extends MentionableParameterBuilder<User, UserParameter, UserParameterBuilder> {

    /**
     * Constructs a new builder with default values.
     */
    @Pure
    public UserParameterBuilder() {}

    /**
     * Constructs a new builder that is a copy of the given builder.
     *
     * @param base The builder to copy.
     */
    @SideEffectFree
    public UserParameterBuilder( final UserParameterBuilder base ) {

        super( base );

    }

    /**
     * Constructs a new builder that is initialized to make a copy of 
     * the given parameter.
     *
     * @param base The parameter to copy.
     */
    @SideEffectFree
    public UserParameterBuilder( final UserParameter base ) {

        super( base );

    }

    @Override
    public UserParameter build() throws IllegalStateException {

        try {
            return new UserParameter( 
                    buildName(),
                    buildDescription(),
                    this.required, this.defaultValue );
        } catch ( final IllegalArgumentException e ) {
            throw new IllegalStateException( "Invalid parameter configuration.", e );
        }

    }
    
}
