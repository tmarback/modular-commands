package dev.sympho.modular_commands.api.permission;

import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.context.AccessContext;
import dev.sympho.modular_commands.api.command.context.ChannelAccessContext;
import reactor.core.publisher.Mono;

/**
 * A group whose membership is defined at the guild level or higher (i.e. the channel is 
 * not taken into account).
 *
 * @version 1.0
 * @since 1.0
 * @apiNote Despite the name, implementations of this interface do not need to take the guild
 *          into account for determining membership; they only cannot use the channel.
 */
@FunctionalInterface
public interface GuildGroup extends Group {

    /**
     * Determines whether the user in the given context belongs to this group.
     *
     * @param context The access context being checked.
     * @return A Mono that issues {@code true} if the user belongs to this group under
     *         the context, or {@code false} otherwise.
     * @implSpec Implementations should prefer using the monos provided by the context directly
     *           instead of obtaining them through other means (such as through the client), as
     *           the context is optimized when possible to reuse existing instances instead of
     *           issuing new API calls.
     */
    @SideEffectFree
    Mono<Boolean> belongs( AccessContext context );

    /**
     * @implSpec Delegates to {@link #belongs(AccessContext)}.
     */
    @Override
    default Mono<Boolean> belongs( final ChannelAccessContext context ) {
        return belongs( ( AccessContext ) context );
    }
    
}
