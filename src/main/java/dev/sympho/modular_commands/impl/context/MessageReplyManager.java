package dev.sympho.modular_commands.impl.context;

import java.util.Objects;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.reply.CommandReplyEditSpec;
import dev.sympho.modular_commands.api.command.reply.CommandReplySpec;
import dev.sympho.modular_commands.api.command.reply.Reply;
import dev.sympho.modular_commands.api.command.reply.ReplyManager;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.PrivateChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

/**
 * Reply manager for message-based commands.
 *
 * @version 1.0
 * @since 1.0
 */
public class MessageReplyManager extends AbstractReplyManager {

    /** The message that triggered the command. */
    private final Message original;
    
    /** Where to send public replies. */
    private final ReplyChain publicChain;
    /** 
     * Where to send private replies. May be the same as {@link #publicChain} if the command
     * is called from a private channel. 
     */
    private final ReplyChain privateChain;

    /**
     * Creates a new manager.
     *
     * @param original The message that triggered the command.
     * @param publicChannel The public channel to send messages in.
     * @param privateChannel The private channel to send messages in. May be {@code null} if the
     *                       command is called from a private channel.
     * @param defaultPrivate Whether replies are private by default.
     * @param deferred Whether the first reply is deferred.
     */
    @SideEffectFree
    public MessageReplyManager( final Message original, 
            final Mono<MessageChannel> publicChannel, 
            final @Nullable Mono<PrivateChannel> privateChannel,
            final boolean defaultPrivate, final boolean deferred ) {

        super( defaultPrivate, deferred );

        this.original = Objects.requireNonNull( original );

        this.publicChain = new ReplyChain( publicChannel, original.getId() );
        this.privateChain = privateChannel == null
                ? this.publicChain // Already in a private channel
                : new ReplyChain( privateChannel, null );

    }

    /**
     * Makes a link to the original message.
     *
     * @return The link.
     */
    private String getOriginalUrl() {

        final var guild = original.getGuildId().map( Snowflake::asString ).orElse( "@me" );
        final var channel = original.getChannelId().asString();
        final var message = original.getId().asString();
        return "https://discord.com/channels/%s/%s/%s".formatted( guild, channel, message );

    }

    @Override
    protected Mono<Reply> send( final int index, final CommandReplySpec spec ) {

        final var channel = spec.privatelyOrElse( defaultPrivate ) ? privateChain : publicChain;
        return channel.send( index, spec.toMessage() );

    }

    @Override
    public ReplyManager longTerm() {

        return this;

    }

    /**
     * A reply made by the manager.
     *
     * @param index The reply index.
     * @param id The ID of the reply message.
     * @param channel The ID of the channel the reply was sent in.
     * @param client The client used to connect to Discord.
     * @since 1.0
     */
    private record MessageReply( 
            int index,
            Snowflake id,
            Snowflake channel,
            GatewayDiscordClient client
    ) implements Reply {

        @Override
        public Mono<Message> message() {

            return client.getMessageById( channel, id );

        }

        @Override
        public Mono<Message> edit( final CommandReplyEditSpec spec ) {

            return message().flatMap( m -> m.edit( spec.toMessage() ) );

        }

        @Override
        public Mono<Void> delete() {

            return message().flatMap( Message::delete );

        }

    }

    /**
     * Manages a sequence of replies sent by the command.
     *
     * @since 1.0
     */
    private class ReplyChain {

        /** The target channel. */
        private final Mono<? extends MessageChannel> channel;
        
        /** The last sent reply ID, or {@code null} if none were sent in this channel yet. */
        private @MonotonicNonNull Snowflake last;

        /**
         * Creates a new instance.
         *
         * @param channel The target channel.
         * @param last The last sent reply ID in this channel, or {@code null} 
         *             if none were sent in this channel yet.
         */
        ReplyChain( final Mono<? extends MessageChannel> channel, 
                final @Nullable Snowflake last ) {

            this.channel = Objects.requireNonNull( channel );
            this.last = last;

        }

        /**
         * Sends reply.
         *
         * @param index The reply index.
         * @param spec The reply specification.
         * @param channel The target channel.
         * @return The sent reply.
         */
        @SuppressWarnings( "HiddenField" )
        private Mono<Reply> send( final int index, final MessageCreateSpec spec, 
                final MessageChannel channel ) {

            final Mono<Snowflake> prevId;
            if ( last == null ) {
                final var reference = EmbedCreateSpec.builder()
                        .color( Color.WHITE )
                        .addField( "command", original.getContent(), false )
                        .addField( "source", getOriginalUrl(), false )
                        .build();
                prevId = channel.createMessage( reference )
                        .map( Message::getId );
            } else {
                prevId = Mono.just( last );
            }

            return prevId.map( spec::withMessageReference )
                .flatMap( channel::createMessage )
                .map( Message::getId )
                .doOnNext( id -> this.last = id )
                .map( id -> new MessageReply( index, id, channel.getId(), channel.getClient() ) );

        }

        /**
         * Sends reply.
         *
         * @param index The reply index.
         * @param spec The reply specification.
         * @return The sent reply.
         */
        public Mono<Reply> send( final int index, final MessageCreateSpec spec ) {

            return channel.flatMap( ch -> send( index, spec, ch ) );

        }

    }
    
}
