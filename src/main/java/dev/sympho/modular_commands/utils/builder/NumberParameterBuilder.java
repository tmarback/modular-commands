package dev.sympho.modular_commands.utils.builder;

import java.util.Map;
import java.util.Objects;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Deterministic;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.context.NumberParameter;

/**
 * Base for a number parameter builder.
 *
 * @param <T> The type of value received by the parameter.
 * @param <P> The parameter type.
 * @param <SELF> The self type.
 * @see NumberParameter
 * @version 1.0
 * @since 1.0
 */
@SuppressWarnings( "HiddenField" )
abstract sealed class NumberParameterBuilder<T extends @NonNull Number & Comparable<T>, 
        P extends @NonNull NumberParameter<T>,
        SELF extends @NonNull NumberParameterBuilder<T, P, SELF>>
        extends ChoicesParameterBuilder<T, P, SELF>
        permits IntegerParameterBuilder, FloatParameterBuilder {

    /** The minimum acceptable value, inclusive. */
    protected @Nullable T minimum;
    /** The maximum acceptable value, inclusive. */
    protected @Nullable T maximum;

    /**
     * Constructs a new builder with default values.
     */
    @Pure
    protected NumberParameterBuilder() {

        this.choices = null;

    }

    /**
     * Constructs a new builder that is a copy of the given builder.
     *
     * @param base The builder to copy.
     */
    @SideEffectFree
    protected NumberParameterBuilder( final NumberParameterBuilder<? extends T, ?, ?> base ) {

        super( base );
        this.minimum = base.minimum;
        this.maximum = base.maximum;

    }

    /**
     * Constructs a new builder that is initialized to make a copy of 
     * the given parameter.
     *
     * @param base The parameter to copy.
     */
    @SideEffectFree
    protected NumberParameterBuilder( final NumberParameter<T> base ) {

        super( base );
        this.minimum = base.minimum();
        this.maximum = base.maximum();

    }

    /**
     * Sets the minimum value of the parameter to build.
     * 
     * <p>If {@link ChoicesParameterBuilder#withChoices(Map) choices} were specified
     * before, they are removed (as if by calling {@link ChoicesParameterBuilder#noChoices()}).
     *
     * @param minimum The minimum value, inclusive. If {@code null}, 
     *                removes the minimum restriction.
     * @return This builder.
     * @see NumberParameter#minimum()
     */
    @Deterministic
    public SELF withMinimum( final @Nullable T minimum ) {

        this.minimum = minimum;
        return noChoices();

    }

    /**
     * Sets the maximum value of the parameter to build.
     * 
     * <p>If {@link ChoicesParameterBuilder#withChoices(Map) choices} were specified
     * before, they are removed (as if by calling {@link ChoicesParameterBuilder#noChoices()}).
     *
     * @param maximum The maximum value, inclusive. If {@code null},
     *                removes the maximum restriction.
     * @return This builder.
     * @see NumberParameter#maximum()
     */
    @Deterministic
    public SELF withMaximum( final @Nullable T maximum ) {

        this.maximum = maximum;
        return noChoices();

    }

    /**
     * Sets the valid range of the parameter to build.
     * 
     * <p>If {@link ChoicesParameterBuilder#withChoices(Map) choices} were specified
     * before, they are removed (as if by calling {@link ChoicesParameterBuilder#noChoices()}).
     *
     * @param minimum The minimum value, inclusive. If {@code null}, 
     *                removes the minimum restriction.
     * @param maximum The maximum value, inclusive. If {@code null},
     *                removes the maximum restriction.
     * @return This builder.
     * @see NumberParameter#minimum()
     * @see NumberParameter#maximum()
     */
    @Deterministic
    public SELF withRange( final @Nullable T minimum, final @Nullable T maximum ) {

        return withMinimum( minimum ).withMaximum( maximum );

    }

    /**
     * Removes the lower bound on the parameter value.
     *
     * @return This builder.
     * @see NumberParameter#minimum()
     */
    @Deterministic
    public SELF noMinimum() {

        this.minimum = null;
        return self();

    }

    /**
     * Removes the upper bound on the parameter value.
     *
     * @return This builder.
     * @see NumberParameter#maximum()
     */
    @Deterministic
    public SELF noMaximum() {

        this.maximum = null;
        return self();

    }

    /**
     * Removes the range bound on the parameter value.
     *
     * @return This builder.
     * @see NumberParameter#minimum()
     * @see NumberParameter#maximum()
     */
    @Deterministic
    public SELF noRange() {

        return noMinimum().noMaximum();

    }

    /**
     * {@inheritDoc}
     * 
     * <p>If a {@link NumberParameterBuilder#withRange(Number, Number) range} was specified
     * before, it is removed (as if by calling {@link NumberParameterBuilder#noRange()}).
     */
    @Override
    public SELF withChoices( final @Nullable Map<String, ? extends T> choices ) {

        return super.withChoices( choices ).noRange();

    }

    /**
     * {@inheritDoc}
     * 
     * <p>If a {@link NumberParameterBuilder#withRange(Number, Number) range} was specified
     * before, it is removed (as if by calling {@link NumberParameterBuilder#noRange()}).
     */
    @Override
    public SELF addChoice( final String name, final T choice ) {

        return super.addChoice( name, choice ).noRange();

    }

    @Override
    public boolean equals( final @Nullable Object o ) {

        if ( o instanceof NumberParameterBuilder<?, ?, ?> b ) {
            return super.equals( o ) && Objects.equals( this.minimum, b.minimum )
                    && Objects.equals( this.maximum, b.maximum );
        } else {
            return false;
        }

    }

    @Override
    public int hashCode() {

        return super.hashCode() ^ Objects.hash( this.minimum, this.maximum );

    }
    
}
