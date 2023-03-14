package dev.sympho.modular_commands.api.command.reply;

import org.immutables.value.Value;

import dev.sympho.modular_commands.utils.SpecStyle;
import discord4j.core.object.entity.Message;
import discord4j.discordjson.MetaEncodingEnabled;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

/**
 * Specification for editing a command reply that can be directly subscribed to to execute.
 *
 * @version 1.0
 * @since 1.0
 */
@SpecStyle
@MetaEncodingEnabled
@Value.Immutable( builder = false )
@SuppressWarnings( { "immutables:subtype", "immutables:incompat" } )
abstract class CommandReplyEditMonoGenerator extends Mono<Message> 
        implements CommandReplyEditSpecGenerator {

    /**
     * The backing reply.
     *
     * @return The reply.
     */
    abstract Reply reply();

    @Override
    @SuppressWarnings( "argument" )
    public void subscribe( final CoreSubscriber<? super Message> actual ) {
        reply().edit( CommandReplyEditSpec.copyOf( this ) ).subscribe( actual );
    }

    @Override
    public abstract String toString();
    
}
