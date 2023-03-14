package dev.sympho.modular_commands.api.command.reply;

import org.immutables.value.Value;

import dev.sympho.modular_commands.utils.SpecStyle;
import discord4j.discordjson.MetaEncodingEnabled;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

/**
 * Specification for creating a new command reply that can be directly subscribed to to execute.
 *
 * @version 1.0
 * @since 1.0
 */
@SpecStyle
@MetaEncodingEnabled
@Value.Immutable( builder = false )
@SuppressWarnings( { "immutables:subtype", "immutables:incompat" } )
abstract class CommandReplyMonoGenerator extends Mono<Reply> implements CommandReplySpecGenerator {

    /**
     * The backing reply manager.
     *
     * @return The manager.
     */
    abstract ReplyManager manager();

    @Override
    @SuppressWarnings( "argument" )
    public void subscribe( final CoreSubscriber<? super Reply> actual ) {
        manager().add( CommandReplySpec.copyOf( this ) ).subscribe( actual );
    }

    @Override
    public abstract String toString();
    
}
