package dev.sympho.modular_commands.api.context;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Specification for an integer parameter.
 *
 * @param name The name of the parameter.
 * @param description The description of the parameter.
 * @param required Whether the parameter must be specified to invoke the command.
 * @param defaultValue The default value for the parameter.
 * @param choices The possible choices for the parameter value.
 * @param minimum The minimum acceptable value (inclusive).
 * @param maximum The maximum acceptable value (inclusive).
 * @version 1.0
 * @since 1.0
 */
public record IntegerParameter( 
        String name, String description, 
        boolean required, @Nullable Long defaultValue, 
        Map<String, Long> choices, 
        Long minimum, Long maximum 
) implements NumberParameter<Long> {

    /**
     * Creates a new instance.
     *
     * <p>Use of this constructor for direct instantiation should be avoided due 
     * to parameter redundancy and lack of parameter checking. Use one of the other 
     * constructors instead.
     *
     * @param name The name of the parameter.
     * @param description The description of the parameter.
     * @param required Whether the parameter must be specified to invoke the command.
     * @param defaultValue The default value for the parameter.
     * @param choices The possible choices for the parameter value.
     * @param minimum The minimum acceptable value (inclusive).
     * @param maximum The maximum acceptable value (inclusive).
     */
    public IntegerParameter( 
            final String name, final String description, 
            final boolean required, final @Nullable Long defaultValue, 
            final Map<String, Long> choices, 
            final Long minimum, final Long maximum
    ) {

        this.name = name;
        this.description = description;
        this.required = required;
        this.defaultValue = defaultValue;
        this.choices = Collections.unmodifiableMap( new HashMap<>( choices ) );
        this.minimum = minimum;
        this.maximum = maximum;

    }

    /**
     * Creates a new instance with no value restrictions.
     *
     * @param name The name of the parameter.
     * @param description The description of the parameter.
     * @param required Whether the parameter must be specified to invoke the command.
     * @param defaultValue The default value for the parameter.
     */
    public IntegerParameter(
            final String name, final String description, 
            final boolean required, final @Nullable Long defaultValue
    ) {
        this( name, description, required, defaultValue, 
                Collections.emptyMap(), Long.MIN_VALUE, Long.MAX_VALUE );
    }

    /**
     * Creates a new instance where the value is restricted to a set of values.
     *
     * @param name The name of the parameter.
     * @param description The description of the parameter.
     * @param required Whether the parameter must be specified to invoke the command.
     * @param defaultValue The default value for the parameter.
     * @param choices The possible choices for the parameter value.
     * @throws IllegalArgumentException if the given set of choices is empty.
     */
    public IntegerParameter(
            final String name, final String description, 
            final boolean required, final @Nullable Long defaultValue,
            final Map<String, Long> choices
    ) throws IllegalArgumentException {
        this( name, description, required, defaultValue, 
                choices, Long.MIN_VALUE, Long.MAX_VALUE );
        if ( choices.isEmpty() ) {
            throw new IllegalArgumentException( "Choices may not be empty" );
        }
    }

    /**
     * Creates a new instance with no value restrictions.
     *
     * @param name The name of the parameter.
     * @param description The description of the parameter.
     * @param required Whether the parameter must be specified to invoke the command.
     * @param defaultValue The default value for the parameter.
     * @param minimum The minimum acceptable value (inclusive).
     * @param maximum The maximum acceptable value (inclusive).
     * @throws IllegalArgumentException if the range is empty (minimum > maximum).
     */
    public IntegerParameter(
            final String name, final String description, 
            final boolean required, final @Nullable Long defaultValue,
            final long minimum, final long maximum
    ) throws IllegalArgumentException {
        this( name, description, required, defaultValue, 
                Collections.emptyMap(), minimum, maximum );
        if ( minimum > maximum ) {
            throw new IllegalArgumentException( "Empty numeric range" );
        }
    }

    @Override
    public Long parseNumber( final String raw ) throws NumberFormatException {

        return Long.parseLong( raw );

    }
    
}
