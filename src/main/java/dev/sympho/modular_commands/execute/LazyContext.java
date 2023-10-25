package dev.sympho.modular_commands.execute;

import dev.sympho.modular_commands.api.command.context.CommandContext;
import dev.sympho.modular_commands.api.command.result.CommandResult;
import io.micrometer.observation.ObservationRegistry;
import reactor.core.publisher.Mono;

/**
 * A command context whose values (particularly arguments) are lazy-loaded (that is, are not
 * loaded until requested by {@link #initialize(ObservationRegistry)} and {@link #load()}).
 * 
 * <p>This interface is not involved in the creation of commands in any way. It is only used
 * internally to decouple initialization/parsing/validation from instantiation and from
 * eachother.
 *
 * @version 1.0
 * @since 1.0
 */
public interface LazyContext extends CommandContext {

    /**
     * Partially initializes internal state, making the context minimally ready for
     * handling to start.
     * 
     * <p>The only parts of the context API that are guaranteed to be ready to use
     * before this method is called are the ones that retrieve pieces of the invocation
     * context, that is {@link #getEvent()}, {@link #caller()}, {@link #getChannel()}, 
     * etc, as well as this method itself. All other methods have undefined behavior.
     * 
     * <p>After this method is called and the returned mono completes <i>successfully</i>,
     * the following pieces of the API also become ready to be used:
     * 
     * <ul>
     *   <li>Reply manager</li>
     *   <li>Access and group checking</li>
     *   <li>{@link #load()}</li>
     * </ul>
     * 
     * <p>Calling any part of the API other than those listed will continue to result in
     * undefined behavior until {@link #load()} is called and successfully completes.
     * 
     * <p>This method is idempotent; if it is called multiple times, the context will still
     * be initialized only once, and all the returned Monos will only complete once it has
     * finished loading.
     *
     * @param observations The registry to use for observations.
     * @return A Mono that completes once the context is initialized.
     */
    Mono<Void> initialize( ObservationRegistry observations );

    /**
     * Loads remaining internal state, making the context fully ready for use. It must only 
     * be called after {@link #initialize(ObservationRegistry)} completes <i>successfully</i>, 
     * otherwise its behavior is undefined.
     * 
     * <p>Until this method is called and the returned mono completes <i>successfully</i>,
     * all methods other than this one (and those specified in 
     * {@link #initialize(ObservationRegistry)} have undefined behavior.
     * 
     * <p>This method is idempotent; if it is called multiple times, the context will still
     * be loaded only once, and all the returned Monos will only complete once it has
     * finished loading, with the same result.
     *
     * @return A Mono that completes empty once internal values are successfully loaded. 
     *         If a situation where the invocation should be terminated is encountered,
     *         it emits the appropriate failure result.
     * @apiNote This is split from {@link #initialize(ObservationRegistry)} for performance 
     *          reasons; only a very limited subset of the API is necessary for early processing 
     *          of the command, and things such as argument parsing can be relatively expensive,
     *          thus it is beneficial to delay the latter until later in the processing
     *          pipeline once they are actually needed.
     */
    Mono<CommandResult> load();
    
}
