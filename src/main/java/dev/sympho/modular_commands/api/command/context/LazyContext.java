package dev.sympho.modular_commands.api.command.context;

import dev.sympho.modular_commands.execute.ResultException;
import reactor.core.publisher.Mono;

/**
 * A command context whose values (particularly arguments) are lazy-loaded (that is, are not
 * loaded until requested by {@link #load()}).
 * 
 * <p>This interface is not involved in the creation of commands in any way. It is only used
 * internally to decouple parsing/validation from instantiation.
 *
 * @version 1.0
 * @since 1.0
 */
public non-sealed interface LazyContext extends CommandContext {

    /**
     * Loads internal state, making the context ready for use.
     * 
     * <p>Until this method is called and the returned mono completes <i>successfully</i>,
     * all methods other than this one have undefined behavior.
     * 
     * <p>This method is idempotent; if it is called multiple times, the context will still
     * be initialized only once, and all the returned Monos will only complete once it has
     * finished initializing.
     *
     * @return A Mono that completes once internal values are loaded. It may also error out
     *         with a {@link ResultException} to terminate the invocation with a result.
     * @throws ResultException if execution should be terminated.
     */
    Mono<Void> load() throws ResultException;
    
}
