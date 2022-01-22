package dev.sympho.modular_commands.utils.builder.parameter;

import java.util.Objects;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Deterministic;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.parameter.Parameter;

/**
 * Base for a parameter builder.
 *
 * @param <T> The type of value received by the parameter.
 * @param <P> The parameter type.
 * @param <SELF> The self type.
 * @see Parameter
 * @version 1.0
 * @since 1.0
 */
@SuppressWarnings( "checkstyle:hiddenfield" )
abstract sealed class ParameterBuilder<
            T extends @NonNull Object, 
            P extends @NonNull Parameter<T>,
            SELF extends @NonNull ParameterBuilder<T, P, SELF>
        > permits ChoicesParameterBuilder {

    /** The parameter name. */
    protected @MonotonicNonNull String name;
    
    /** The parameter description. */
    protected @MonotonicNonNull String description;

    /** Whether the parameter is required. */
    protected boolean required;
    
    /** The default parameter value. */
    protected @Nullable T defaultValue;

    /**
     * Constructs a new builder with default values.
     */
    @Pure
    protected ParameterBuilder() {

        this.name = null;
        this.description = null;
        this.required = false;
        this.defaultValue = null;

    }

    /**
     * Constructs a new builder that is a copy of the given builder.
     *
     * @param base The builder to copy.
     */
    @SideEffectFree
    protected ParameterBuilder( final ParameterBuilder<? extends T, ?, ?> base ) {

        this.name = base.name;
        this.description = base.description;
        this.required = base.required;
        this.defaultValue = base.defaultValue;

    }

    /**
     * Constructs a new builder that is initialized to make a copy of 
     * the given parameter.
     *
     * @param base The parameter to copy.
     */
    @SideEffectFree
    protected ParameterBuilder( final Parameter<T> base ) {

        this.name = base.name();
        this.description = base.description();
        this.required = base.required();
        this.defaultValue = base.defaultValue();

    }

    /**
     * Retrieves {@code this} cast to the self type.
     *
     * @return {@code this}.
     */
    @Deterministic
    protected SELF self() {

        @SuppressWarnings( "unchecked" )
        final SELF self = ( SELF ) this;
        return self;

    }

    /**
     * Sets the name of the parameter to build.
     *
     * @param name The parameter name.
     * @return This builder.
     * @see Parameter#name()
     */
    @Deterministic
    public SELF withName( final String name ) {

        this.name = Objects.requireNonNull( name, "Name cannot be null." );
        return self();

    }

    /**
     * Sets the description of the parameter to build.
     *
     * @param description The parameter description.
     * @return This builder.
     * @see Parameter#description()
     */
    @Deterministic
    public SELF withDescription( final String description ) {

        this.description = Objects.requireNonNull( description, "Description cannot be null." );
        return self();

    }

    /**
     * Sets the required flag of the parameter to build.
     *
     * @param required The required flag.
     * @return This builder.
     * @see Parameter#required()
     */
    @Deterministic
    public SELF withRequired( final boolean required ) {

        this.required = required;
        return self();

    }

    /**
     * Sets the default value of the parameter to build.
     *
     * @param defaultValue The default value.
     * @return This builder.
     * @see Parameter#defaultValue()
     */
    @Deterministic
    public SELF withDefault( final @Nullable T defaultValue ) {

        this.defaultValue = defaultValue;
        return self();

    }

    /**
     * Retrieve the name to use for building, after error checking.
     *
     * @return The name to build with.
     * @throws IllegalStateException if {@link #name} was not set.
     */
    protected String buildName() throws IllegalStateException {
    
        if ( this.name == null ) {
            throw new IllegalStateException( 
                    "Parameter name must be set before building." );
        } else {
            return this.name;
        }
    
    }

    /**
     * Retrieve the description to use for building, after error checking.
     *
     * @return The description to build with.
     * @throws IllegalStateException if {@link #description} was not set.
     */
    protected String buildDescription() throws IllegalStateException {
    
        if ( this.description == null ) {
            throw new IllegalStateException( 
                    "Parameter description must be set before building." );
        } else {
            return this.description;
        }
    
    }

    /**
     * Builds the configured parameter.
     *
     * @return The built parameter.
     * @throws IllegalStateException if the current configuration is invalid.
     */
    @SideEffectFree
    public abstract P build() throws IllegalStateException;

    /**
     * Indicates whether the given object is a builder of the same type and
     * configuration as this one.
     */
    @Override
    public boolean equals( final @Nullable Object o ) {

        if ( o instanceof ParameterBuilder<?, ?, ?> b ) {
            return Objects.equals( this.name, b.name )
                    && Objects.equals( this.description, b.description )
                    && this.required == b.required
                    && Objects.equals( this.defaultValue, b.defaultValue );
        } else {
            return false;
        }

    }

    @Override
    public int hashCode() {

        return Objects.hash( this.name, this.description, this.required, this.defaultValue );

    }
    
}
