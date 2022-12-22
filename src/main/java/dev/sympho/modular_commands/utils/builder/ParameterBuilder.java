package dev.sympho.modular_commands.utils.builder;

import java.util.Objects;

import org.checkerframework.checker.calledmethods.qual.CalledMethods;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.common.returnsreceiver.qual.This;
import org.checkerframework.common.value.qual.MatchesRegex;
import org.checkerframework.dataflow.qual.Deterministic;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.parameter.Parameter;
import dev.sympho.modular_commands.api.command.parameter.parse.ArgumentParser;
import dev.sympho.modular_commands.utils.ParameterUtils;

/**
 * Base for a parameter builder.
 *
 * @param <T> The type of value received by the parameter.
 * @version 1.0
 * @since 1.0
 */
@SuppressWarnings( "checkstyle:hiddenfield" )
public final class ParameterBuilder<T extends @NonNull Object> {

    /** The parameter name. */
    protected @MonotonicNonNull @MatchesRegex( Parameter.NAME_REGEX ) String name;
    
    /** The parameter description. */
    protected @MonotonicNonNull @MatchesRegex( Parameter.DESCRIPTION_REGEX ) String description;

    /** Whether the parameter is required. */
    protected boolean required;
    
    /** The default parameter value. */
    protected @Nullable T defaultValue;

    /** The parser to use for processing. */
    protected @MonotonicNonNull ArgumentParser<?, T> parser;

    /**
     * Constructs a new builder with default values.
     */
    @Pure
    public ParameterBuilder() {

        this.name = null;
        this.description = null;
        this.required = false;
        this.defaultValue = null;
        this.parser = null;

    }

    /**
     * Constructs a new builder that is a copy of the given builder.
     *
     * @param base The builder to copy.
     */
    @SideEffectFree
    public ParameterBuilder( final ParameterBuilder<T> base ) {

        this.name = base.name;
        this.description = base.description;
        this.required = base.required;
        this.defaultValue = base.defaultValue;
        this.parser = base.parser;

    }

    /**
     * Constructs a new builder that is initialized to make a copy of 
     * the given parameter.
     *
     * @param <T> The argument type.
     * @param base The parameter to copy.
     * @return The initialized builder.
     * @throws IllegalArgumentException if the given parameter has invalid values.
     */
    @SideEffectFree
    public static <T extends @NonNull Object> @CalledMethods( {
            "withName", "withDescription", 
            "withRequired", "withDefault", 
            "withParser" 
    } ) ParameterBuilder<T> from( final Parameter<T> base ) throws IllegalArgumentException {

        return new ParameterBuilder<T>()
                .withName( base.name() )
                .withDescription( base.description() )
                .withRequired( base.required() )
                .withDefault( base.defaultValue() )
                .withParser( base.parser() );

    }

    /**
     * Sets the name of the parameter to build.
     *
     * @param name The parameter name.
     * @return This builder.
     * @throws IllegalArgumentException if the given name is invalid.
     * @see Parameter#name()
     */
    @Deterministic
    public @This ParameterBuilder<T> withName( 
            final @MatchesRegex( Parameter.NAME_REGEX ) String name ) 
            throws IllegalArgumentException {

        this.name = ParameterUtils.validateName( name );
        return this;

    }

    /**
     * Sets the description of the parameter to build.
     *
     * @param description The parameter description.
     * @return This builder.
     * @throws IllegalArgumentException if the given name is invalid.
     * @see Parameter#description()
     */
    @Deterministic
    public @This ParameterBuilder<T> withDescription( 
            final @MatchesRegex( Parameter.DESCRIPTION_REGEX ) String description ) 
            throws IllegalArgumentException {

        this.description = ParameterUtils.validateDescription( description );
        return this;

    }

    /**
     * Sets the required flag of the parameter to build.
     *
     * @param required The required flag.
     * @return This builder.
     * @see Parameter#required()
     */
    @Deterministic
    public @This ParameterBuilder<T> withRequired( final boolean required ) {

        this.required = required;
        return this;

    }

    /**
     * Sets the default value of the parameter to build.
     *
     * @param defaultValue The default value.
     * @return This builder.
     * @see Parameter#defaultValue()
     */
    @Deterministic
    public @This ParameterBuilder<T> withDefault( final @Nullable T defaultValue ) {

        this.defaultValue = defaultValue;
        return this;

    }

    /**
     * Sets the parser to process arguments with.
     *
     * @param parser The parser to use.
     * @return This builder.
     * @see Parameter#parser()
     */
    @Deterministic
    public @This ParameterBuilder<T> withParser( final ArgumentParser<?, T> parser ) {

        this.parser = Objects.requireNonNull( parser );
        return this;

    }

    /**
     * Builds the configured parameter.
     *
     * @return The built parameter.
     * @throws IllegalStateException if the current configuration is invalid.
     */
    @SideEffectFree
    public Parameter<T> build( 
            @CalledMethods( { "withName", "withDescription", "withParser" } ) 
            ParameterBuilder<T> this
    ) throws IllegalStateException {

        if ( this.name == null ) {
            throw new IllegalStateException( "Parameter name must be set before building." );
        }
        if ( this.description == null ) {
            throw new IllegalStateException( "Parameter description must be set before building." );
        }
        if ( this.parser == null ) {
            throw new IllegalStateException( "Parameter parser must be set before building." );
        }

        return new Parameter<>( name, description, required, defaultValue, parser );

    }

    /**
     * Indicates whether the given object is a builder of the same type and
     * configuration as this one.
     */
    @Override
    public boolean equals( final @Nullable Object o ) {

        if ( o instanceof ParameterBuilder<?> b ) {
            return Objects.equals( this.name, b.name )
                    && Objects.equals( this.description, b.description )
                    && this.required == b.required
                    && Objects.equals( this.defaultValue, b.defaultValue )
                    && Objects.equals( this.parser, b.parser );
        } else {
            return false;
        }

    }

    @Override
    public int hashCode() {

        return Objects.hash( 
                this.name, this.description, 
                this.required, this.defaultValue, 
                this.parser 
        );

    }
    
}
