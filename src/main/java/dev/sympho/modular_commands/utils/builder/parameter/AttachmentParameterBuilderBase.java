package dev.sympho.modular_commands.utils.builder.parameter;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.common.value.qual.IntRange;
import org.checkerframework.dataflow.qual.Deterministic;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.parameter.AttachmentParameter;
import dev.sympho.modular_commands.utils.SizeUtils;


/**
 * Base for an attachment parameter builder.
 *
 * @param <T> The type of value received by the parameter.
 * @param <P> The parameter type.
 * @param <SELF> The self type.
 * @see AttachmentParameter
 * @version 1.0
 * @since 1.0
 */
@SuppressWarnings( "checkstyle:hiddenfield" )
abstract sealed class AttachmentParameterBuilderBase<
                T extends @NonNull Object, 
                P extends @NonNull AttachmentParameter<T>,
                SELF extends @NonNull AttachmentParameterBuilderBase<T, P, SELF>
        > extends ParameterBuilder<T, P, SELF> permits TextFileParameterBuilder {

    /** The maximum file size. */
    protected @IntRange( from = 0 ) int maxSize;

    /**
     * Constructs a new builder with default values.
     */
    @Pure
    protected AttachmentParameterBuilderBase() {

        this.maxSize = Integer.MAX_VALUE;

    }

    /**
     * Constructs a new builder that is a copy of the given builder.
     *
     * @param base The builder to copy.
     */
    @SideEffectFree
    protected AttachmentParameterBuilderBase( 
            final AttachmentParameterBuilderBase<? extends T, ?, ?> base ) {

        super( base );
        this.maxSize = base.maxSize;

    }

    /**
     * Constructs a new builder that is initialized to make a copy of 
     * the given parameter.
     *
     * @param base The parameter to copy.
     */
    @SideEffectFree
    protected AttachmentParameterBuilderBase( final AttachmentParameter<T> base ) {

        super( base );
        this.maxSize = base.maxSize();

    }

    /**
     * Sets the maximum accepted file size.
     *
     * @param maxSize The maximum accepted size (in bytes).
     * @return This builder.
     * @see AttachmentParameter#maxSize()
     * @see SizeUtils
     */
    @Deterministic
    public SELF withMaxSize( final @IntRange( from = 0 ) int maxSize ) {

        this.maxSize = maxSize;
        return self();

    }
            
}
