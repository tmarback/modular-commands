package dev.sympho.modular_commands.api.command.context;

import org.checkerframework.checker.nullness.qual.Nullable;

import dev.sympho.bot_utils.event.MessageCreateEventContext;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.MessageChannel;
import reactor.core.publisher.Mono;

/**
 * The execution context of an command invoked through a message.
 *
 * @version 1.0
 * @since 1.0
 */
public interface MessageCommandContext extends CommandContext, MessageCreateEventContext {

    @Override
    default Mono<MessageChannel> channel() {
        return MessageCreateEventContext.super.channel();
    }

    @Override
    default @Nullable Member callerMember() {
        return event().getMember().orElse( null );
    }
    
}
