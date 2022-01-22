package dev.sympho.modular_commands.utils.builder.parameter;

import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.parameter.FloatParameter;

/**
 * Builder for a floating-point parameter.
 *
 * @see FloatParameter
 * @version 1.0
 * @since 1.0
 */
public final class FloatParameterBuilder 
        extends NumberParameterBuilder<Double, FloatParameter, FloatParameterBuilder> {

    /**
     * Constructs a new builder with default values.
     */
    @Pure
    protected FloatParameterBuilder() {}

    /**
     * Constructs a new builder that is a copy of the given builder.
     *
     * @param base The builder to copy.
     */
    @SideEffectFree
    protected FloatParameterBuilder( final FloatParameterBuilder base ) {

        super( base );

    }

    /**
     * Constructs a new builder that is initialized to make a copy of 
     * the given parameter.
     *
     * @param base The parameter to copy.
     */
    @SideEffectFree
    protected FloatParameterBuilder( final FloatParameter base ) {

        super( base );

    }

    @Override
    public FloatParameter build() throws IllegalStateException {

        try {
            return new FloatParameter( 
                    buildName(),
                    buildDescription(),
                    this.required, this.defaultValue,
                    this.choices, this.minimum, this.maximum );
        } catch ( final IllegalArgumentException e ) {
            throw new IllegalStateException( "Invalid parameter configuration.", e );
        }

    }
    
}
