package dev.sympho.modular_commands.execute;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import org.checkerframework.checker.nullness.qual.Nullable;

import dev.sympho.modular_commands.api.command.Command;
import dev.sympho.modular_commands.api.command.result.Results;
import discord4j.core.event.domain.Event;
import discord4j.core.object.entity.ApplicationInfo;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.rest.util.PermissionSet;
import reactor.core.publisher.Mono;

/**
 * Validator that checks that a command's invocation is appropriate
 * as per the command's defined parameters.
 *
 * @param <E> The type of event that triggers the commands.
 * @version 1.0
 * @since 1.0
 */
public abstract class InvocationValidator<E extends Event> {

    /**
     * The validators used to validate a command's settings during
     * invocation.
     */
    private final List<Validator<?>> validators = List.<Validator<?>>of(
        new NsfwValidator(),
        new ServerOwnerValidator(),
        new BotOwnerValidator()
    );

    /**
     * Retrieves the command's caller from the triggering event.
     *
     * @param event The triggering event.
     * @return The user that invoked the command.
     */
    protected abstract User getCaller( E event );

    /**
     * Retrieves the channel where the command was invoked from the
     * triggering event.
     *
     * @param event The triggering event.
     * @return The channel where the event was invoked.
     */
    protected abstract Mono<MessageChannel> getChannel( E event );

    /**
     * Retrieves the guild where the command was invoked from the
     * triggering event.
     *
     * @param event The triggering event.
     * @return The guild where the event was invoked.
     */
    protected abstract Mono<Guild> getGuild( E event );

    /**
     * Validates that a command invocation is appropriate as per the command's
     * settings.
     *
     * @param event The triggering event.
     * @param chain The execution chain.
     * @return A Mono that completes successfully if all requirements were satisfied
     *         or disabled for that command, otherwise issuing a {@link ResultException}
     *         error with an error result.
     */
    public Mono<Void> validateSettings( final E event, final List<? extends Command> chain ) {

        final Command command = InvocationUtils.getSettingsSource( chain );        

        Mono<Void> state = Mono.empty();
        for ( final var validator : validators ) {
            state = state.thenEmpty( validator.validate( command, event ) );
        }
        return state;

    }

    /**
     * Retreives the effective permissions that the given user has in the given channel.
     *
     * @param channel The channel.
     * @param user The user.
     * @return The effective permissions for the given user in the given channel.
     */
    private Mono<PermissionSet> getChannelPermissions( final MessageChannel channel, 
            final User user ) {

        if ( channel instanceof GuildChannel c ) {
            return c.getEffectivePermissions( user.getId() );
        } else {
            return Mono.empty();
        }

    }

    /**
     * Validates that the user that invoked a command has sufficient permissions to do so.
     *
     * @param event The triggering event.
     * @param chain The execution chain.
     * @return A Mono that completes successfully if the user posses the required permissions,
     *         otherwise issuing a {@link ResultException} error with an error result.
     */
    public Mono<Void> validatePermissions( final E event, 
            final List<? extends Command> chain ) {

        final var required = InvocationUtils.accumulatePermissions( chain );
        if ( required.isEmpty() ) {
            return Mono.empty();
        }

        return getChannel( event )
                .flatMap( c -> getChannelPermissions( c, getCaller( event ) ) )
                .map( required::andNot )
                .filter( Predicate.not( Collection::isEmpty ) )
                .map( m -> "Missing permissions: " + m.toString() )
                .map( Results::failure )
                .doOnNext( r -> {
                    throw new ResultException( r );
                } )
                .then();
        
    }

    /**
     * A validator for a command property.
     *
     * @param <T> The type of object used in validation.
     * @version 1.0
     * @since 1.0
     */
    private abstract class Validator<T> {

        /**
         * Determines if the validator is active per the configuration of the
         * given command.
         *
         * @param command The command.
         * @return {@code true} if the given command specifies that this validator should
         *         be active, {@code false} otherwise. 
         */
        protected abstract boolean active( Command command );

        /**
         * Retrives the value necessary for validation from the triggering event.
         *
         * @param event The triggering event.
         * @return A Mono that issues the necessary value.
         */
        protected abstract Mono<T> getValue( E event );

        /**
         * Validates that the requirement is met.
         *
         * @param caller The user that called the command.
         * @param value The validation value.
         * @return {@code null} if the invocation is valid, otherwise an error message.
         */
        protected abstract @Nullable String validate( User caller, T value );

        /**
         * Validates that the requirement is satisfied or not enabled for the given command
         * and triggering event.
         *
         * @param command The command being invoked.
         * @param event The triggering event.
         * @return A Mono that completes successfully if the requirement was satisfied
         *         or disabled for that command, otherwise issuing a {@link ResultException}
         *         error with an error result.
         */
        public Mono<Void> validate( final Command command, final E event ) {

            if ( active( command ) ) {
                return getValue( event )
                        .mapNotNull( v -> validate( getCaller( event ), v ) )
                        .map( Results::failure )
                        .doOnNext( r -> {
                            throw new ResultException( r );
                        } )
                        .then();
            } else {
                return Mono.empty();
            }

        }

    }

    /**
     * A validator for commands that may only be executed in NSFW channels.
     *
     * @version 1.0
     * @since 1.0
     */
    private final class NsfwValidator extends Validator<MessageChannel> {

        /** Creates a new instance. */
        NsfwValidator() {}
         
        @Override
        public boolean active( final Command command ) {
            return command.nsfw();
        }

        @Override
        public Mono<MessageChannel> getValue( final E event ) {
            return getChannel( event );
        }

        @Override
        public @Nullable String validate( final User caller, final MessageChannel channel ) {

            if ( channel instanceof TextChannel ch && ch.isNsfw() ) {
                return null;
            } else {
                return "Command can only be called from a NSFW channel.";
            }

        }

    }

    /**
     * A validator for commands that may only be executed by server owners.
     *
     * @version 1.0
     * @since 1.0
     */
    private final class ServerOwnerValidator extends Validator<Guild> {

        /** Creates a new instance. */
        ServerOwnerValidator() {}
         
        @Override
        public boolean active( final Command command ) {
            return command.serverOwnerOnly();
        }

        @Override
        public Mono<Guild> getValue( final E event ) {
            return getGuild( event );
        }

        @Override
        public @Nullable String validate( final User caller, final Guild guild ) {

            if ( guild.getOwnerId().equals( caller.getId() ) ) {
                return null;
            } else {
                return "Must be server owner to call this command.";
            }

        }

    }

    /**
     * A validator for commands that may only be executed by the bot owner.
     *
     * @version 1.0
     * @since 1.0
     */
    private final class BotOwnerValidator extends Validator<ApplicationInfo> {

        /** Creates a new instance. */
        BotOwnerValidator() {}
         
        @Override
        public boolean active( final Command command ) {
            return command.botOwnerOnly();
        }

        @Override
        public Mono<ApplicationInfo> getValue( final E event ) {
            return event.getClient().getApplicationInfo();
        }

        @Override
        public @Nullable String validate( final User caller, final ApplicationInfo guild ) {

            if ( guild.getOwnerId().equals( caller.getId() ) ) {
                return null;
            } else {
                return "Must be bot owner to call this command.";
            }

        }

    }
    
}
