package dev.sympho.modular_commands.api.context;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.checkerframework.checker.nullness.qual.Nullable;

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
     * @param choices The possible choices for the parameter value.
     */
    public StringParameter( 
            final String name, final String description, 
            final boolean required, final @Nullable String defaultValue, 
            final Map<String, String> choices
    ) {

        this.name = name;
        this.description = description;
        this.required = required;
        this.defaultValue = defaultValue;
        this.choices = Collections.unmodifiableMap( new HashMap<>( choices ) );

    }

    /**
     * Creates a new instance with no value restrictions.
     *
     * @param name The name of the parameter.
     * @param description The description of the parameter.
     * @param required Whether the parameter must be specified to invoke the command.
     * @param defaultValue The default value for the parameter.
     */
    public StringParameter(
            final String name, final String description, 
            final boolean required, final @Nullable String defaultValue
    ) {
        this( name, description, required, defaultValue, Collections.emptyMap() );
    }

    @Override
    public String parseValue( final String raw ) throws IllegalArgumentException {

        return raw;

    }
    
}
