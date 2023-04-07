package dev.sympho.modular_commands.execute;

import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.context.CommandContext;
import reactor.core.publisher.Mono;

/**
 * A command context that is instrumented for use with metrics and tracing tooling.
 *
 * @version 1.0
 * @since 1.0
 * @apiNote If also implementing {@link LazyContext}, this API should be available before any 
 *          methods are called.
 */
public interface InstrumentedContext extends CommandContext {

    /**
     * Retrieves the ID of the invoked command.
     *
     * @return The command ID.
     */
    @Pure
    String getCommandId();

    /**
     * Determines the value for the {@link Metrics.Tag.Type type tag}.
     *
     * @return The tag.
     */
    @Pure
    Metrics.Tag.Type tagType();

    /**
     * Adds the common instrumentation tags for this context to a Mono.
     *
     * @param <T> The mono value type.
     * @param mono The mono to add tags to.
     * @return The mono with tags added.
     */
    @SideEffectFree
    default <T> Mono<T> addTags( final Mono<T> mono ) {

        return mono
                .transform( tagType()::apply )
                .transform( Metrics.Tag.CommandInvocation.called( getInvocation() )::apply )
                .transform( Metrics.Tag.CommandInvocation.canonical( 
                        getCommandInvocation() )::apply )
                .transform( Metrics.Tag.CommandId.from( getCommandId() )::apply );

    }
    
}
