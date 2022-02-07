package dev.sympho.modular_commands.execute;

import dev.sympho.modular_commands.api.registry.Registry;
import discord4j.core.GatewayDiscordClient;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

/**
 * Executor that receives events and invokes triggered commands as appropriate.
 *
 * @version 1.0
 * @since 1.0
 */
public class CommandExecutor {

    /** The event processing pipeline. */
    private final Mono<Void> pipeline;

    /** The current processing task. */
    private @Nullable Disposable live;

    /**
     * Creates a new instance.
     *
     * @param client The client to receive events from.
     * @param registry The registry to use to look up commands.
     * @param builder The builder to use for constructing the event processing pipeline.
     */
    protected CommandExecutor( final GatewayDiscordClient client, final Registry registry,
            final PipelineBuilder<?, ?, ?, ?, ?> builder ) {

        pipeline = builder.buildPipeline( client, registry );

    }

    /* Public interface methods */

    /**
     * Start receiving events and executing commands.
     *
     * @return {@code true} if the call started the executor, 
     *         {@code false} if it was already started.
     */
    public synchronized boolean start() {

        if ( live != null ) {
            return false;
        }

        live = pipeline.subscribe();
        return true;

    }

    /**
     * Stops receiving events and executing commands.
     *
     * @return {@code true} if the call stopped the executor, 
     *         {@code false} if it was already stopped.
     */
    public synchronized boolean stop() {

        if ( live == null ) {
            return false;
        }

        live.dispose();
        live = null;
        return true;

    }
    
}
