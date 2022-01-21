package dev.sympho.modular_commands.api.command.context;

import org.checkerframework.checker.nullness.qual.Nullable;

import dev.sympho.modular_commands.api.command.parameter.Parameter;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.interaction.ApplicationCommandInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import reactor.core.publisher.Mono;

/**
 * A class that can represent any context type.
 * 
 * <p>This class should never be used and only exists to avoid code duplication in
 * other parts of the API because Java does not have a true bottom type.
 * 
 * <p>It is also the reason why {@link #getEvent()} doesn't just get a narrower
 * return type in each subinterface instead of getting a bunch of clones.
 * 
 * <p>Yes I hate it too but there's nothing I can do about it.
 *
 * @version 1.0
 * @since 1.0
 */
public final class AnyCommandContext implements MessageCommandContext, SlashCommandContext {

    /** Do not instantiate. */
    private AnyCommandContext() {}

    @Override
    public User getCaller() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @Nullable Member getCallerMember() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Mono<MessageChannel> getChannel() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Snowflake getChannelId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Mono<Guild> getGuild() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @Nullable Snowflake getGuildId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> @Nullable T getArgument( final String name, 
            Class<? extends Parameter<T>> parameterType )
            throws IllegalArgumentException, ClassCastException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean setContext( final String key, final @Nullable Object obj, 
            final boolean replace ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> @Nullable T getContext( final String key, final Class<T> type )
            throws IllegalArgumentException, ClassCastException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Event getEvent() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ApplicationCommandInteractionEvent getInteractionEvent() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChatInputInteractionEvent getSlashEvent() {
        throw new UnsupportedOperationException();
    }

    @Override
    public MessageCreateEvent getMessageEvent() {
        throw new UnsupportedOperationException();
    }
    
}
