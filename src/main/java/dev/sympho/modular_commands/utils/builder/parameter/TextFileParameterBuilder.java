package dev.sympho.modular_commands.utils.builder.parameter;

import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.parameter.TextFileParameter;

/**
 * Builder for a text file parameter.
 *
 * @see TextFileParameter
 * @version 1.0
 * @since 1.0
 */
public final class TextFileParameterBuilder extends AttachmentParameterBuilderBase<String, 
        TextFileParameter, TextFileParameterBuilder> {

    /**
     * Constructs a new builder with default values.
     */
    @Pure
    public TextFileParameterBuilder() {}

    /**
     * Constructs a new builder that is a copy of the given builder.
     *
     * @param base The builder to copy.
     */
    @SideEffectFree
    public TextFileParameterBuilder( 
            final TextFileParameterBuilder base ) {

        super( base );

    }

    /**
     * Constructs a new builder that is initialized to make a copy of 
     * the given parameter.
     *
     * @param base The parameter to copy.
     */
    @SideEffectFree
    public TextFileParameterBuilder( final TextFileParameter base ) {

        super( base );

    }

    @Override
    public TextFileParameter build() throws IllegalStateException {

        try {
            return new TextFileParameter( 
                    buildName(),
                    buildDescription(),
                    this.required, this.defaultValue,
                    this.maxSize );
        } catch ( final IllegalArgumentException e ) {
            throw new IllegalStateException( "Invalid parameter configuration.", e );
        }

    }
    
}
