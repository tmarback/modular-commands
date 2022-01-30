package dev.sympho.modular_commands.utils.builder;

import org.checkerframework.dataflow.qual.Deterministic;

/**
 * Base type for a builder.
 *
 * @param <SELF> The self type to be returned in all chain methods.
 * @version 1.0
 * @since 1.0
 * @apiNote Be very careful when using this. {@code SELF} <b>MUST</b> be the type of the
 *          builder itself, or at least compatible with the return type of the chain
 *          builder methods, else it will cause an exception when assigned to a variable
 *          (and the compiler won't be able to warn about it).
 */
public interface Builder<SELF extends Builder<SELF>> {

    /**
     * Retrieves {@code this} cast to the self type.
     *
     * @return {@code this}.
     * @apiNote This does <i>not</i> check that the cast is valid. Make sure that
     *          {@code SELF} is set to the appropriate type.
     */
    @Deterministic
    @SuppressWarnings( "unchecked" )
    default SELF self() {

        return ( SELF ) this;

    }
    
}
