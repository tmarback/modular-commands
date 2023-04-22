package dev.sympho.modular_commands.execute;

import org.checkerframework.dataflow.qual.SideEffectFree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.util.annotation.Nullable;

/**
 * Executor that receives events and invokes triggered commands as appropriate.
 *
 * @version 1.0
 * @since 1.0
 */
public abstract class CommandExecutor {

    /** The logger. */
    protected final Logger logger = LoggerFactory.getLogger( this.getClass() );

    /** The current processing task. */
    private @Nullable Disposable live;

    /**
     * Creates a new instance.
     */
    protected CommandExecutor() {}

    /**
     * Constructs the processing pipeline that processes command events.
     *
     * @return The constructed pipeline.
     */
    @SideEffectFree
    protected abstract Flux<?> buildPipeline();

    /* Lifecycle methods */

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

        logger.info( "Starting command executor" );
        live = buildPipeline().subscribe();
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

        logger.info( "Stopping command executor" );
        live.dispose();
        live = null;
        return true;

    }
    
}
