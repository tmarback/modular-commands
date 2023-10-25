package dev.sympho.modular_commands.api.command.context;

import org.checkerframework.checker.nullness.qual.Nullable;

import dev.sympho.bot_utils.event.InteractionEventContext;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.MessageChannel;
import reactor.core.publisher.Mono;

/**
 * The execution context of an command invoked through an interaction (application command).
 *
 * @version 1.0
 * @since 1.0
 */
public interface InteractionCommandContext extends CommandContext, InteractionEventContext {

    @Override
    default Mono<MessageChannel> channel() {
        return InteractionEventContext.super.channel();
    }

    @Override
    default Mono<Member> member() {
        return CommandContext.super.member();
    }

    @Override
    default Mono<Member> member( final Snowflake guildId ) {
        return CommandContext.super.member( guildId );
    }

    @Override
    default @Nullable Member callerMember() {
        return event().getInteraction().getMember().orElse( null );
    }
    
}
