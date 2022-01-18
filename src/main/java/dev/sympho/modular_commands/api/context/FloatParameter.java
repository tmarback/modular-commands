package dev.sympho.modular_commands.api.context;

import java.util.Map;
import java.util.Objects;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.SideEffectFree;

/**
 * Specification for an floating-point parameter.
 * 
 * <p>Note that NaN is not allowed in any part of the specification.
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
public record FloatParameter( 
        String name, String description, 
        boolean required, @Nullable Double defaultValue, 
        Map<String, Double> choices, 
        Double minimum, Double maximum 
) implements NumberParameter<Double> {

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
     * @throws IllegalArgumentException if {@link Double#NaN} is used for any value, if
     *         the provided range is empty (minimum > maximum), or if a set of choices
     *         is provided but is empty.
     * @apiNote Use of this constructor for direct instantiation should be avoided due 
     *          to parameter redundancy.
     */
    @SideEffectFree
    public FloatParameter( 
            final String name, final String description, 
            final boolean required, final @Nullable Double defaultValue, 
            final @Nullable Map<String, Double> choices, 
            final @Nullable Double minimum, final @Nullable Double maximum
    ) {

        this.name = Objects.requireNonNull( name, "Name cannot be null." );
        this.description = Objects.requireNonNull( description, "Description cannot be null." );
        this.required = required;
        this.defaultValue = defaultValue;
        this.choices = ContextUtils.validateChoices( choices );
        this.minimum = Objects.requireNonNullElse( minimum, Double.NEGATIVE_INFINITY );
        this.maximum = Objects.requireNonNullElse( maximum, Double.POSITIVE_INFINITY );

        if ( this.minimum.isNaN() || this.maximum.isNaN() 
                || this.choices.values().stream().anyMatch( c -> c.isNaN() ) ) {
            throw new IllegalArgumentException( "NaN is not a valid parameter value." );
        }
        if ( this.minimum.doubleValue() > this.maximum.doubleValue() ) {
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
    public FloatParameter(
            final String name, final String description, 
            final boolean required, final @Nullable Double defaultValue
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
     * @throws IllegalArgumentException if the given set of choices is empty or contains
     *                                  {@link Double#NaN}.
     */
    @SideEffectFree
    public FloatParameter(
            final String name, final String description, 
            final boolean required, final @Nullable Double defaultValue,
            final Map<String, Double> choices
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
     * @throws IllegalArgumentException if {@link Double#NaN} is used for any value or
     *                                  if the range is empty (minimum > maximum).
     */
    @SideEffectFree
    public FloatParameter(
            final String name, final String description, 
            final boolean required, final @Nullable Double defaultValue,
            final double minimum, final double maximum
    ) throws IllegalArgumentException {
        this( name, description, required, defaultValue, null, minimum, maximum );
    }

    @Override
    public Double parseNumber( final String raw ) throws NumberFormatException {

        return Double.parseDouble( raw );

    }
    
}
