package dev.sympho.modular_commands.api.command.context;

import org.checkerframework.checker.nullness.qual.Nullable;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import reactor.core.publisher.Mono;

/**
 * An access context that copies an external context but overrides the user 
 * (and associated values).
 *
 * @param <C> The context type.
 * @version 1.0
 * @since 1.0
 */
class UserOverrideAccessContext<C extends AccessContext> implements AccessContext {

    /** The original context. */
    protected final C base;
    /** The user to override to. */
    protected final User user;

    /**
     * Creates a new instance.
     *
     * @param base The original context.
     * @param user The user to override to.
     */
    UserOverrideAccessContext( final C base, final User user ) {

        this.user = user;
        this.base = base;

    }

    @Override
    public GatewayDiscordClient getClient() {
        return base.getClient();
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public Mono<Guild> getGuild() {
        return base.getGuild();
    }

    @Override
    public @Nullable Snowflake getGuildId() {
        return base.getGuildId();
    }

    @Override
    public boolean isPrivate() {
        return base.isPrivate();
    }

}
