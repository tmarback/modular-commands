package dev.sympho.modular_commands.execute;

import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.permission.AccessValidator;
import dev.sympho.modular_commands.api.permission.Group;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import reactor.core.publisher.Mono;

/**
 * A manager that provides access validators for different execution contexts.
 *
 * @version 1.0
 * @since 1.0
 */
@FunctionalInterface
public interface AccessManager {

    /**
     * Creates an access validator under the context of the given guild, channel, and caller.
     *
     * @param guild The guild of the current execution.
     * @param channel The channel of the current execution.
     * @param caller The caller that invoked the execution.
     * @return The appropriate access validator.
     */
    AccessValidator validator( Mono<Guild> guild, Mono<MessageChannel> channel, User caller );

    /**
     * Creates a manager for which all validators always allow access.
     * That is, a manager that allows access for any user to any group (effectively disabling
     * group checking).
     *
     * @return The manager.
     */
    @SideEffectFree
    static AccessManager alwaysAllow() {

        return ( guild, channel, caller ) -> group -> Mono.just( true );

    }

    /**
     * Creates a manager for which all validators always deny access.
     * That is, a manager that denies access for any user to any group (effectively disabling
     * any functionality that requires group membership).
     *
     * @return The manager.
     */
    @SideEffectFree
    static AccessManager alwaysDeny() {

        return ( guild, channel, caller ) -> group -> Mono.just( false );

    }

    /**
     * Creates a manager that issues validators that perform simple group memebership checks
     * (that is, a user has equivalent access to a group if and only if they belong to that
     * group).
     *
     * @return The manager.
     */
    @SideEffectFree
    static AccessManager basic() {

        return ( guild, channel, caller ) -> group -> group.belongs( guild, channel, caller );

    }

    /**
     * Creates a manager that issues validators that generally perform group membership checks,
     * but also allows a given group to override any permission in the system (that is, users
     * that are members of the given group have equivalent permissions to any other group).
     *
     * @param overrideGroup The group that overrides any permission check.
     * @return The manager.
     */
    @SideEffectFree
    static AccessManager overridable( final Group overrideGroup ) {

        return ( guild, channel, caller ) -> group -> group.belongs( guild, channel, caller )
                .filter( Boolean::booleanValue ) // Make empty if not allowed
                .switchIfEmpty( Mono.defer( // If not allowed, check override
                        () -> overrideGroup.belongs( guild, channel, caller ) 
                ) )
                .defaultIfEmpty( false );

    }
    
}
