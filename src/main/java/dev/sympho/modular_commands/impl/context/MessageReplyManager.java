package dev.sympho.modular_commands.impl.context;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.ReplyManager;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.PrivateChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.core.spec.MessageEditSpec;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * Reply manager for message-based commands.
 *
 * @version 1.0
 * @since 1.0
 */
public class MessageReplyManager implements ReplyManager {

    /** The initial capacity of the reply list. */
    private static final int INITIAL_CAPACITY = 5;

    /** The message that triggered the command. */
    private final Message original;
    /** The public channel to send messages in. */
    private final MessageChannel publicChannel;
    /** 
     * The private channel to send messages in. 
     * Can be {@code null} in case it would be the same as the public
     * channel (which would make it redundant).
     */
    private final @MonotonicNonNull PrivateChannel privateChannel;

    /** The replies sent so far. */
    private final List<Reply> replies;

    /** The ID if the last message sent in the public channel for the chain. */
    private Snowflake lastPublic;
    /** The ID if the last message sent in the private channel for the chain. */
    private @MonotonicNonNull Snowflake lastPrivate;

    /** Whether messages are to be sent in the private channel. */
    private boolean priv;
    /** The ephemeral modifier to use. */
    private EphemeralType ephemeral;
    /** The delay before timed messages are to be deleted. */
    private Duration delay;

    /** Whether the first message is to be deferred. */
    private boolean deferred;

    /**
     * Creates a new manager.
     *
     * @param original The message that triggered the command.
     * @param publicChannel The public channel to send messages in.
     * @param privateChannel The private channel to send messages in. 
     */
    @SideEffectFree
    public MessageReplyManager( final Message original, 
            final MessageChannel publicChannel, final PrivateChannel privateChannel ) {

        this.original = Objects.requireNonNull( original );

        this.publicChannel = Objects.requireNonNull( publicChannel );
        if ( publicChannel.getId().equals( Objects.requireNonNull( privateChannel ).getId() ) ) {
            this.privateChannel = null; // Already in a private channel
        } else {
            this.privateChannel = privateChannel;
        }

        this.replies = new ArrayList<>( INITIAL_CAPACITY );

        this.lastPublic = original.getId();
        this.lastPrivate = null;

        this.priv = false;
        this.ephemeral = EphemeralType.NONE;
        this.delay = Duration.ZERO;

        this.deferred = false;

    }

    /**
     * Gets the channel to send messages in.
     *
     * @return The channel.
     */
    @Pure
    private MessageChannel getChannel() {

        if ( priv && privateChannel != null ) {
            return privateChannel;
        } else {
            return publicChannel;
        }

    }

    /**
     * Make a link to the original message.
     *
     * @return The link.
     */
    private String getOriginalUrl() {

        final var guild = original.getGuildId().map( Snowflake::asString ).orElse( "@me" );
        final var channel = publicChannel.getId().asString();
        final var message = original.getId().asString();
        return "https://discord.com/channels/%s/%s/%s".formatted( guild, channel, message );

    }

    @Override
    @SuppressWarnings( "HiddenField" )
    public ReplyManager setPrivate( final boolean priv ) {

        if ( privateChannel != null ) {
            this.priv = priv; 
        } // Else public is already private
        return this;

    }

    @Override
    @SuppressWarnings( "HiddenField" )
    public ReplyManager setEphemeral( final EphemeralType ephemeral ) {

        this.ephemeral = Objects.requireNonNull( ephemeral );
        return this;

    }

    @Override
    @SuppressWarnings( "HiddenField" )
    public ReplyManager setDeleteDelay( final Duration delay ) {

        this.delay = Objects.requireNonNull( delay );
        return this;

    }

    @Override
    public Mono<Void> defer() {

        if ( deferred ) {
            return Mono.empty();
        } else {
            deferred = true;
            return getChannel().type();
        }

    }

    @Override
    public Mono<Void> reply( final MessageCreateSpec spec ) throws IllegalStateException {

        if ( !replies.isEmpty() ) {
            throw new IllegalStateException( "Reply already sent." );
        }
        return add( spec ).then();

    }

    @Override
    public Mono<Tuple2<Message, Integer>> add( final MessageCreateSpec spec ) {

        final var channel = getChannel();
        // https://github.com/typetools/checker-framework/issues/1256
        @SuppressWarnings( "monotonic" )
        final @Nullable Snowflake previous = priv ? lastPrivate : lastPublic;

        final Mono<Snowflake> prevId;
        if ( previous == null ) {
            final var reference = EmbedCreateSpec.builder()
                    .color( Color.WHITE )
                    .description( original.getContent() )
                    .footer( getOriginalUrl(), null )
                    .build();
            prevId = channel.createMessage( reference )
                    .map( Message::getId );
        } else {
            prevId = Mono.just( previous );
        }

        return prevId.map( id -> MessageCreateSpec.builder()
                        .from( spec )
                        .messageReference( id )
                        .build() 
                )
                .flatMap( channel::createMessage )
                .map( m -> {

                    final var id = replies.size();
                    replies.add( new Reply( channel, m.getId() ) );

                    if ( priv ) {
                        lastPrivate = m.getId();
                    } else {
                        lastPublic = m.getId();
                    }

                    if ( ephemeral.timed() ) { // Delete after delay
                        m.delete().delaySubscription( delay ).subscribe();
                    }

                    return Tuples.of( m, id );

                } );

    }

    @Override
    public Mono<Message> edit( final int index, final MessageEditSpec spec )
            throws IndexOutOfBoundsException, IllegalStateException {

        return get( index ).flatMap( m -> m.edit( spec ) );

    }

    @Override
    public Mono<Message> get( final int index ) 
            throws IndexOutOfBoundsException, IllegalStateException {

        return replies.get( index ).message();

    }

    @Override
    public Mono<Void> delete( final int index ) 
            throws IndexOutOfBoundsException, IllegalStateException {

        return get( index ).flatMap( Message::delete );

    }

    @Override
    public ReplyManager longTerm() {

        return this;

    }

    /**
     * A reply made by a manager.
     *
     * @param channel The channel the reply was sent in.
     * @param id The ID of the reply message.
     * @version 1.0
     * @since 1.0
     */
    private record Reply( MessageChannel channel, Snowflake id ) {

        /**
         * Retrieves the message.
         *
         * @return The reply message.
         */
        public Mono<Message> message() {

            return channel.getMessageById( id );

        }

    }
    
}
