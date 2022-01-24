package dev.sympho.modular_commands.api.command.parameter;

import java.util.Map;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.SideEffectFree;

/**
 * Specification for a string parameter.
 *
 * @param name The name of the parameter.
 * @param description The description of the parameter.
 * @param required Whether the parameter must be specified to invoke the command.
 * @param defaultValue The default value for the parameter.
 * @param choices The possible choices for the parameter value.
 * @version 1.0
 * @since 1.0
 */
public record StringParameter( 
        String name, String description, 
        boolean required, @Nullable String defaultValue, 
        Map<String, String> choices
) implements ChoicesParameter<String> {

    /**
     * Creates a new instance where the value is restricted to a set of values.
     *
     * @param name The name of the parameter.
     * @param description The description of the parameter.
     * @param required Whether the parameter must be specified to invoke the command.
     * @param defaultValue The default value for the parameter.
     * @param choices The possible choices for the parameter value. If {@code null},
     *                the value is not restricted to a set.
     * @throws IllegalArgumentException if a set of choices is proved but is empty,
     *                                  or one of the choices or names is an empty
     *                                  string.
     */
    @SideEffectFree
    public StringParameter( 
            final String name, final String description, 
            final boolean required, final @Nullable String defaultValue, 
            final @Nullable Map<String, String> choices
    ) {

        this.name = ParameterUtils.validateName( name );
        this.description = ParameterUtils.validateDescription( description );
        this.required = required;
        this.defaultValue = defaultValue;
        this.choices = ParameterUtils.validateChoices( choices );

        if ( this.choices.values().stream().anyMatch( String::isEmpty ) ) {
            throw new IllegalArgumentException( "A choice cannot be empty." );
        }

    }

    /**
     * Creates a new instance with no value restrictions.
     *
     * @param name The name of the parameter.
     * @param description The description of the parameter.
     * @param required Whether the parameter must be specified to invoke the command.
     * @param defaultValue The default value for the parameter.
     */
    @SideEffectFree
    public StringParameter(
            final String name, final String description, 
            final boolean required, final @Nullable String defaultValue
    ) {
        this( name, description, required, defaultValue, null );
    }

    @Override
    public String parseValue( final String raw ) throws IllegalArgumentException {

        return raw;

    }
    
}
