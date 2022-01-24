package dev.sympho.modular_commands.api.command.parameter;

import java.util.Map;
import java.util.Objects;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.SideEffectFree;

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
     * <p>While specifying both a set of choices and a valid range is allowed, it
     * is discouraged due to being a redundant condition while opening the possiblity
     * of issues if one of the conditions is updated without updating the other.
     *
     * @param name The name of the parameter.
     * @param description The description of the parameter.
     * @param required Whether the parameter must be specified to invoke the command.
     * @param defaultValue The default value for the parameter.
     * @param choices The possible choices for the parameter value. If {@code null},
     *                the value is not restricted to a set.
     * @param minimum The minimum acceptable value (inclusive). If {@code null},
     *                there is no minimum.
     * @param maximum The maximum acceptable value (inclusive). If {@code null},
     *                there is no maximum.
     * @throws IllegalArgumentException if the provided range is empty 
     *                                  (minimum > maximum), or if a set of choices is 
     *                                  provided but is empty.
     * @apiNote Use of this constructor for direct instantiation should be avoided due 
     *          to parameter redundancy.
     */
    @SideEffectFree
    public IntegerParameter( 
            final String name, final String description, 
            final boolean required, final @Nullable Long defaultValue, 
            final @Nullable Map<String, Long> choices, 
            final @Nullable Long minimum, final @Nullable Long maximum
    ) {

        this.name = ParameterUtils.validateName( name );
        this.description = ParameterUtils.validateDescription( description );
        this.required = required;
        this.defaultValue = defaultValue;
        this.choices = ParameterUtils.validateChoices( choices );
        this.minimum = Objects.requireNonNullElse( minimum, Long.MIN_VALUE );
        this.maximum = Objects.requireNonNullElse( maximum, Long.MAX_VALUE );

        if ( this.minimum.longValue() > this.maximum.longValue() ) {
            throw new IllegalArgumentException( "Empty numeric range" );
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
    public IntegerParameter(
            final String name, final String description, 
            final boolean required, final @Nullable Long defaultValue
    ) {
        this( name, description, required, defaultValue, null, null, null );
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
    @SideEffectFree
    public IntegerParameter(
            final String name, final String description, 
            final boolean required, final @Nullable Long defaultValue,
            final Map<String, Long> choices
    ) throws IllegalArgumentException {
        this( name, description, required, defaultValue, choices, null, null );
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
    @SideEffectFree
    public IntegerParameter(
            final String name, final String description, 
            final boolean required, final @Nullable Long defaultValue,
            final long minimum, final long maximum
    ) throws IllegalArgumentException {
        this( name, description, required, defaultValue, null, minimum, maximum );
    }

    @Override
    public Long parseNumber( final String raw ) throws NumberFormatException {

        return Long.parseLong( raw );

    }
    
}
