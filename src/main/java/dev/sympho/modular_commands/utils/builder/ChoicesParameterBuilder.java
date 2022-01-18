package dev.sympho.modular_commands.utils.builder;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Deterministic;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.parameter.ChoicesParameter;

/**
 * Base for a choices parameter builder.
 *
 * @param <T> The type of value received by the parameter.
 * @param <P> The parameter type.
 * @param <SELF> The self type.
 * @see ChoicesParameter
 * @version 1.0
 * @since 1.0
 */
@SuppressWarnings( "HiddenField" )
abstract sealed class ChoicesParameterBuilder<T extends @NonNull Object, 
        P extends dev.sympho.modular_commands.api.command.parameter.ChoicesParameter<T>,
        SELF extends @NonNull ChoicesParameterBuilder<T, P, SELF>>
        extends ParameterBuilder<T, P, SELF>
        permits NumberParameterBuilder, StringParameterBuilder {

    /** The valid user choices. */
    protected @Nullable Map<String, T> choices;

    /**
     * Constructs a new builder with default values.
     */
    @Pure
    protected ChoicesParameterBuilder() {

        this.choices = null;

    }

    /**
     * Constructs a new builder that is a copy of the given builder.
     *
     * @param base The builder to copy.
     */
    @SideEffectFree
    protected ChoicesParameterBuilder( final ChoicesParameterBuilder<? extends T, ?, ?> base ) {

        super( base );
        this.choices = base.choices == null ? null : new HashMap<>( base.choices );

    }

    /**
     * Constructs a new builder that is initialized to make a copy of 
     * the given parameter.
     *
     * @param base The parameter to copy.
     */
    @SideEffectFree
    protected ChoicesParameterBuilder( final ChoicesParameter<T> base ) {

        super( base );
        this.choices = base.choices().isEmpty() ? null : new HashMap<>( base.choices() );

    }

    /**
     * Sets the possible choices of the parameter to build.
     *
     * @param choices The parameter choices. If {@code null}, removes all choices
     *                (makes parameter value unrestricted).
     * @return This builder.
     * @see ChoicesParameter#choices()
     */
    @Deterministic
    public SELF withChoices( final @Nullable Map<String, ? extends T> choices ) {

        this.choices = choices == null ? null : new HashMap<>( choices );
        return self();

    }

    /**
     * Adds a possible of the parameter to build.
     *
     * @param name The name of the choice.
     * @param value The value of the choice.
     * @return This builder.
     * @see ChoicesParameter#choices()
     */
    @Deterministic
    public SELF addChoice( final String name, final T value ) {

        if ( this.choices == null ) {
            this.choices = new HashMap<>();
        }
        this.choices.put( name, value );
        return self();

    }

    /**
     * Clears the possible choices, but <i>does not</i> disable restricting
     * the value with choices.
     *
     * @return This builder.
     * @see ChoicesParameter#choices()
     */
    @Deterministic
    public SELF clearChoices() {

        this.choices = new HashMap<>();
        return self();

    }

    /**
     * Disables restricting the value with choices.
     *
     * @return This builder.
     * @see ChoicesParameter#choices()
     */
    @Deterministic
    public SELF noChoices() {

        this.choices = null;
        return self();

    }

    @Override
    public boolean equals( final @Nullable Object o ) {

        if ( o instanceof ChoicesParameterBuilder<?, ?, ?> b ) {
            return super.equals( o ) && Objects.equals( this.choices, b.choices );
        } else {
            return false;
        }

    }

    @Override
    public int hashCode() {

        return super.hashCode() ^ Objects.hashCode( this.choices );

    }
    
}
