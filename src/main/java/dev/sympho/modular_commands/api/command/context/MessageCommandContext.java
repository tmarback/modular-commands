package dev.sympho.modular_commands.api.command.context;

import java.util.List;
import java.util.Map;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;

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

    /**
     * Retrieves the raw argument string as it was received, before being split.
     * 
     * <p>Does not include the command name(s) (see {@link CommandContext#invocation()} for that),
     * only the arguments that are later used for {@link #rawArgs()}.
     *
     * @return The received arguments before splitting.
     */
    @Pure
    String argString();

    /**
     * Retrieves the raw arguments received before parsing, in the order that they were received.
     *
     * @return The raw arguments received. The returned list is unmodifiable.
     */
    @Pure
    List<String> rawArgs();

    /**
     * Retrieves the raw arguments received, keyed by the corresponding parameter name.
     *
     * @return The raw arguments received. The returned map is unmodifiable.
     */
    @Pure
    Map<String, String> rawArgMap();
    
}
