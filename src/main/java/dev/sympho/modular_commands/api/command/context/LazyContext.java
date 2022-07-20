package dev.sympho.modular_commands.api.command.context;

import dev.sympho.modular_commands.execute.ResultException;
import reactor.core.publisher.Mono;

/**
 * A command context whose values (particularly arguments) are lazy-loaded (that is, are not
 * loaded until requested by {@link #initialize()} and {@link #load()}).
 * 
 * <p>This interface is not involved in the creation of commands in any way. It is only used
 * internally to decouple initialization/parsing/validation from instantiation and from
 * eachother.
 *
 * @version 1.0
 * @since 1.0
 */
public non-sealed interface LazyContext extends CommandContext {

    /**
     * Partially initializes internal state, making the context minimally ready for
     * handling to start.
     * 
     * <p>The only parts of the context API that are guaranteed to be ready to use
     * after this method is called are:
     * 
     * <ul>
     *   <li>Invocation context (event, caller, channel, etc)</li>
     *   <li>Reply manager</li>
     *   <li>Access and group checking</li>
     *   <li>{@link #load()}</li>
     * </ul>
     * 
     * <p>Until this method is called and the returned mono completes <i>successfully</i>,
     * all methods other than this one have undefined behavior. Calling any part of the
     * API other than those listed here will continue to result in undefined behavior
     * until {@link #load()} is called and successfully completes.
     * 
     * <p>This method is idempotent; if it is called multiple times, the context will still
     * be initialized only once, and all the returned Monos will only complete once it has
     * finished loading.
     *
     * @return A Mono that completes once the context is initialized.
     */
    Mono<Void> initialize();

    /**
     * Loads remaining internal state, making the context fully ready for use. It must only 
     * be called after {@link #initialize()} completes <i>successfully</i>, otherwise its 
     * behavior is undefined.
     * 
     * <p>Until this method is called and the returned mono completes <i>successfully</i>,
     * all methods other than this one have undefined behavior.
     * 
     * <p>This method is idempotent; if it is called multiple times, the context will still
     * be loaded only once, and all the returned Monos will only complete once it has
     * finished loading.
     *
     * @return A Mono that completes once internal values are loaded. It may also error out
     *         with a {@link ResultException} to terminate the invocation with a result.
     * @throws ResultException if execution should be terminated.
     * @apiNote This is split from {@link #initialize()} for performance reasons; only a
     *          very limited subset of the API is necessary for early processing of the
     *          command, and things such as argument parsing can be relatively expensive,
     *          thus it is beneficial to delay the latter until later in the processing
     *          pipeline once they are actually needed.
     */
    Mono<Void> load() throws ResultException;
    
}
