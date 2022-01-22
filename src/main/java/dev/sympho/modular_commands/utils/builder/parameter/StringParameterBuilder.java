package dev.sympho.modular_commands.utils.builder.parameter;

import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.parameter.StringParameter;

/**
 * Builder for a string parameter.
 *
 * @see StringParameter
 * @version 1.0
 * @since 1.0
 */
public final class StringParameterBuilder 
        extends ChoicesParameterBuilder<String, StringParameter, StringParameterBuilder> {

    /**
     * Constructs a new builder with default values.
     */
    @Pure
    protected StringParameterBuilder() {}

    /**
     * Constructs a new builder that is a copy of the given builder.
     *
     * @param base The builder to copy.
     */
    @SideEffectFree
    protected StringParameterBuilder( final StringParameterBuilder base ) {

        super( base );

    }

    /**
     * Constructs a new builder that is initialized to make a copy of 
     * the given parameter.
     *
     * @param base The parameter to copy.
     */
    @SideEffectFree
    protected StringParameterBuilder( final StringParameter base ) {

        super( base );

    }

    @Override
    public StringParameter build() throws IllegalStateException {

        try {
            return new StringParameter( 
                    buildName(),
                    buildDescription(),
                    this.required, this.defaultValue,
                    this.choices );
        } catch ( final IllegalArgumentException e ) {
            throw new IllegalStateException( "Invalid parameter configuration.", e );
        }

    }
    
}
