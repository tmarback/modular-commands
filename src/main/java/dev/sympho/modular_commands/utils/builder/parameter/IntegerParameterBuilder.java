package dev.sympho.modular_commands.utils.builder.parameter;

import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.parameter.IntegerParameter;

/**
 * Builder for an integer parameter.
 *
 * @see IntegerParameter
 * @version 1.0
 * @since 1.0
 */
public final class IntegerParameterBuilder 
        extends NumberParameterBuilder<Long, IntegerParameter, IntegerParameterBuilder> {

    /**
     * Constructs a new builder with default values.
     */
    @Pure
    protected IntegerParameterBuilder() {}

    /**
     * Constructs a new builder that is a copy of the given builder.
     *
     * @param base The builder to copy.
     */
    @SideEffectFree
    protected IntegerParameterBuilder( final IntegerParameterBuilder base ) {

        super( base );

    }

    /**
     * Constructs a new builder that is initialized to make a copy of 
     * the given parameter.
     *
     * @param base The parameter to copy.
     */
    @SideEffectFree
    protected IntegerParameterBuilder( final IntegerParameter base ) {

        super( base );

    }

    @Override
    public IntegerParameter build() throws IllegalStateException {

        try {
            return new IntegerParameter( 
                    buildName(),
                    buildDescription(),
                    this.required, this.defaultValue,
                    this.choices, this.minimum, this.maximum );
        } catch ( final IllegalArgumentException e ) {
            throw new IllegalStateException( "Invalid parameter configuration.", e );
        }

    }
    
}
