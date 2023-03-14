package dev.sympho.modular_commands.impl.context;

import java.util.ArrayList;
import java.util.List;

import dev.sympho.modular_commands.api.command.reply.CommandReplySpec;
import dev.sympho.modular_commands.api.command.reply.Reply;
import dev.sympho.modular_commands.api.command.reply.ReplyManager;
import dev.sympho.reactor_utils.concurrent.AsyncLock;
import dev.sympho.reactor_utils.concurrent.ReactiveLock;
import reactor.core.publisher.Mono;

/**
 * Base implementation for a reply manager.
 *
 * @version 1.0
 * @since 1.0
 */
abstract class AbstractReplyManager implements ReplyManager {

    /** The sent replies. */
    protected final List<Reply> replies;
    /** The lock for send ordering. */
    protected final ReactiveLock sendLock;

    /** Whether replies are private by default. */
    protected final boolean defaultPrivate;
    /** Whether the first reply is deferred. */
    protected final boolean deferred;

    /**
     * Creates a new instance.
     *
     * @param defaultPrivate Whether replies are private by default.
     * @param deferred Whether the first reply is deferred.
     */
    AbstractReplyManager( final boolean defaultPrivate, final boolean deferred ) {

        this.defaultPrivate = defaultPrivate;
        this.deferred = deferred;

        this.replies = new ArrayList<>();
        this.sendLock = new AsyncLock();

    }

    /**
     * Sends a new reply.
     *
     * @param index The index of the created reply.
     * @param spec The reply specification.
     * @return The created reply.
     * @implSpec This method does not need to manage concurrency. This class ensures that
     *           each call to it only occurs after the previous reply is complete.
     */
    protected abstract Mono<Reply> send( int index, CommandReplySpec spec );

    @Override
    public Mono<Reply> add( final CommandReplySpec spec ) {

        return Mono.fromSupplier( () -> deferred && replies.isEmpty()
                        ? spec.withPrivately( defaultPrivate )
                        : spec
                )
                .flatMap( s -> send( replies.size(), s ) )
                .doOnNext( replies::add )
                .transform( sendLock::guard );

    }

    @Override
    public Reply get( final int index ) throws IndexOutOfBoundsException {

        return replies.get( index );

    }
    
}
