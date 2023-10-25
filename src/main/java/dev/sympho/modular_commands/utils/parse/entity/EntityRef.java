package dev.sympho.modular_commands.utils.parse.entity;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.context.CommandContext;
import dev.sympho.modular_commands.api.command.parameter.parse.InvalidArgumentException;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Entity;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Channel;
import reactor.core.publisher.Mono;

/**
 * A reference to a Discord entity that can be used to fetch the entity.
 *
 * @param <E> The entity type.
 * @version 1.0
 * @since 1.0
 */
@FunctionalInterface
public interface EntityRef<E extends @NonNull Entity> {

    /**
     * Obtains the referenced entity.
     *
     * @param client The client to use to connect to Discord.
     * @return The entity. May be empty if there is no entity that matches the reference, and
     *         may fail with a {@link InvalidArgumentException} if an entity exists but there
     *         is a mismatch.
     * @since 1.0
     */
    @SideEffectFree
    Mono<E> get( GatewayDiscordClient client );

    /**
     * A reference to a user.
     *
     * @param id The user ID.
     * @since 1.0
     */
    record UserRef(
            Snowflake id
    ) implements EntityRef<User> {

        @Override
        public Mono<User> get( final GatewayDiscordClient client ) {

            return client.getUserById( id );

        }
    
    }

    /**
     * A reference to a role.
     *
     * @param id The role ID.
     * @param guildId The ID of the guild that the role exists in.
     * @since 1.0
     */
    record RoleRef(
            Snowflake id,
            Snowflake guildId
    ) implements EntityRef<Role> {

        /**
         * Creates a new instance when only the ID is specified.
         *
         * @param context The execution context.
         * @param id The role ID.
         * @throws InvalidArgumentException if the invocation was not in a guild.
         * @implSpec Other values default to where the command was invoked in.
         */
        public RoleRef( final CommandContext context, final Snowflake id ) 
                throws InvalidArgumentException {

            this( id, defaultGuild( context ) );

        }

        /**
         * Obtains the default guild based on the context.
         *
         * @param context The execution context.
         * @return The guild ID.
         * @throws InvalidArgumentException if the invocation was not in a guild.
         */
        @Pure
        public static Snowflake defaultGuild( final CommandContext context ) 
                throws InvalidArgumentException {

            final var guild = context.guildId();
            if ( guild == null ) {
                throw new InvalidArgumentException( 
                        "Currently in a private channel, please use a URL" 
                );
            }
            return guild;

        }

        /**
         * Obtains the ID for the {@code @everyone} role.
         *
         * @param context The execution context.
         * @return The {@code @everyone} role ID.
         * @throws InvalidArgumentException if the invocation was not in a guild.
         */
        @Pure
        public static Snowflake everyoneId( final CommandContext context ) 
                throws InvalidArgumentException {

            return defaultGuild( context );

        }

        @Override
        public Mono<Role> get( final GatewayDiscordClient client ) {

            return client.getRoleById( guildId, id );

        }

    }

    /**
     * A reference to a channel.
     *
     * @param <C> The channel type.
     * @param type The channel type.
     * @param id The channel ID.
     * @since 1.0
     */
    record ChannelRef<C extends @NonNull Channel>(
            Class<C> type,
            Snowflake id
    ) implements EntityRef<C> {

        @Override
        public Mono<C> get( final GatewayDiscordClient client ) {

            return client.getChannelById( id )
                    .cast( type )
                    .onErrorMap( ClassCastException.class, ex -> new InvalidArgumentException( 
                            "Channel must be a " + type.getSimpleName() 
                    ) );

        }

        @Override
        public String toString() {

            return new StringFormat( type.getSimpleName(), id ).toString();

        }

        /**
         * Carrier type to make toString look right.
         *
         * @param type The type name.
         * @param id The ID.
         * @since 1.0
         */
        private record StringFormat( String type, Snowflake id ) {}

    }

    /**
     * A reference to a message.
     *
     * @param id The message ID.
     * @param channelId The ID of the channel that the role exists in.
     * @since 1.0
     */
    record MessageRef(
            Snowflake id,
            Snowflake channelId
    ) implements EntityRef<Message> {

        /**
         * Creates a new instance when only the ID is specified.
         *
         * @param context The execution context.
         * @param id The role ID.
         * @implSpec Other values default to where the command was invoked in.
         */
        public MessageRef( final CommandContext context, final Snowflake id ) {
            this( id, context.channelId() );
        }

        @Override
        public Mono<Message> get( final GatewayDiscordClient client ) {

            return client.getMessageById( channelId, id );

        }

    }

}
