package dev.sympho.modular_commands.api.permission;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Default group implementations and composition operations.
 *
 * @version 1.0
 * @since 1.0
 */
public final class Groups {

    /** The group of all Discord users (matches everyone). */
    public static final Group EVERYONE = named( 
            ( guild, channel, user ) -> Mono.just( true ), 
            "Everyone" 
    );

    /** The empty group (matches nobody). */
    public static final Group NOBODY = named( 
            ( guild, channel, user ) -> Mono.just( false ),
            "Nobody"
    );

    /**
     * The group of server admins (matches users with the 
     * {@link Permission#ADMINISTRATOR administrator} permission).
     * 
     * <p>Note that this group does <i>not</i> match when in private channels.
     */
    public static final Group ADMINS = named(
            hasGuildPermissions( PermissionSet.of( Permission.ADMINISTRATOR ), true ),
            "Administrators"
    );

    /** Do not instantiate. */
    private Groups() {}

    /**
     * Adds a name to an existing group.
     *
     * @param group The group.
     * @param name The name.
     * @return The given group with the name set to the given name.
     */
    public static NamedGroup named( final Group group, final String name ) {

        return new Named( group, name );

    }

    public static Group any( final Flux<Group> groups ) {

        return ( guild, channel, caller ) -> {

            return groups.flatMap( g -> g.belongs( guild, channel, caller) )
                    .any( Boolean::booleanValue );

        };

    }

    public static Group any( final Stream<Group> groups ) {
        return any( Flux.fromStream( groups ) );
    }

    public static Group any( final Iterable<Group> groups ) {
        return any( Flux.fromIterable( groups ) );
    }

    public static Group any( final Group... groups ) {
        return any( Flux.fromArray( groups ) );
    }

    public static Group all( final Flux<Group> groups ) {

        return ( guild, channel, caller ) -> {

            return groups.flatMap( g -> g.belongs( guild, channel, caller) )
                    .all( Boolean::booleanValue );

        };

    }

    public static Group all( final Stream<Group> groups ) {
        return all( Flux.fromStream( groups ) );
    }

    public static Group all( final Iterable<Group> groups ) {
        return all( Flux.fromIterable( groups ) );
    }

    public static Group all( final Group... groups ) {
        return all( Flux.fromArray( groups ) );
    }

    public static Group isUser( final Snowflake user ) {
        return new UserSingle( user );
    }

    public static Group isUser( final Collection<Snowflake> users ) {
        return new Users( users );
    }

    public static Group isUser( final Snowflake... users ) {
        return isUser( Arrays.asList( users ) );
    }

    public static Group hasRole( final Snowflake role ) {
        return new RoleSingle( role );
    }

    public static Group hasRolesAny( final Collection<Snowflake> roles ) {
        return new RolesAny( roles );
    }

    public static Group hasRolesAny( final Snowflake... roles ) {
        return hasRolesAny( Arrays.asList( roles ) );
    }

    public static Group hasRolesAll( final Collection<Snowflake> roles ) {
        return new RolesAll( roles );
    }

    public static Group hasRolesAll( final Snowflake... roles ) {
        return hasRolesAll( Arrays.asList( roles ) );
    }

    public static Group hasGuildPermissions( final PermissionSet permissions, 
            final boolean allowPrivate ) {
        return new PermissionsGuild( permissions, allowPrivate );
    }

    public static Group hasGuildPermissions( final PermissionSet permissions ) {
        return hasGuildPermissions( permissions, true );
    }

    public static Group hasChannelPermissions( final PermissionSet permissions, 
            final boolean allowPrivate ) {
        return new PermissionsChannel( permissions, allowPrivate );
    }

    public static Group hasChannelPermissions( final PermissionSet permissions ) {
        return hasChannelPermissions( permissions, true );
    }

    private record Named( Group group, String name ) implements NamedGroup {

        @Override
        public Mono<Boolean> belongs( Mono<Guild> guild, Mono<MessageChannel> channel,
                User caller ) {

            return group.belongs( guild, channel, caller );

        }

        @Override
        public String name() {

            return name;

        }

    }

    private record UserSingle( Snowflake user ) implements Group {

        @Override
        public Mono<Boolean> belongs( final Mono<Guild> guild, final Mono<MessageChannel> channel,
                final User caller ) {

            return Mono.just( user.equals( caller.getId() ) );

        }

    }

    private record Users( Collection<Snowflake> users ) implements Group {

        public Users( final Collection<Snowflake> users ) {
            this.users = Set.copyOf( users );
        }

        @Override
        public Mono<Boolean> belongs( final Mono<Guild> guild, final Mono<MessageChannel> channel,
                final User caller ) {

            return Mono.just( users.contains( caller.getId() ) );

        }

    }

    private record RoleSingle( Snowflake role ) implements Group {

        @Override
        public Mono<Boolean> belongs( final Mono<Guild> guild, final Mono<MessageChannel> channel,
                final User caller ) {

            return guild.flatMap( g -> g.getMemberById( caller.getId() ) )
                    .flatMapMany( m -> m.getRoles() )
                    .map( Role::getId )
                    .any( role::equals );

        }

    }

    private record RolesAny( Collection<Snowflake> roles ) implements Group {

        public RolesAny( final Collection<Snowflake> roles ) {
            this.roles = Set.copyOf( roles );
        }

        @Override
        public Mono<Boolean> belongs( final Mono<Guild> guild, final Mono<MessageChannel> channel,
                final User caller ) {

            return guild.flatMap( g -> g.getMemberById( caller.getId() ) )
                    .flatMapMany( m -> m.getRoles() )
                    .map( Role::getId )
                    .any( roles::contains );

        }

    }

    private record RolesAll( Collection<Snowflake> roles ) implements Group {

        public RolesAll( final Collection<Snowflake> roles ) {
            this.roles = Set.copyOf( roles );
        }

        @Override
        public Mono<Boolean> belongs( final Mono<Guild> guild, final Mono<MessageChannel> channel,
                final User caller ) {

            return guild.flatMap( g -> g.getMemberById( caller.getId() ) )
                    .flatMapMany( m -> m.getRoles() )
                    .map( Role::getId )
                    .collect( Collectors.toSet() )
                    .map( r -> r.containsAll( roles ) );

        }

    }

    private record PermissionsGuild( PermissionSet permissions, boolean allowPrivate ) 
            implements Group {

        @Override
        public Mono<Boolean> belongs( final Mono<Guild> guild, final Mono<MessageChannel> channel,
                final User caller ) {

            return guild.flatMap( g -> g.getMemberById( caller.getId() ) )
                    .flatMap( m -> m.getBasePermissions() )
                    .map( p -> p.containsAll( permissions ) )
                    .defaultIfEmpty( allowPrivate ); // Not in a guild

        }

    }

    private record PermissionsChannel( PermissionSet permissions, boolean allowPrivate ) 
            implements Group {

        @Override
        public Mono<Boolean> belongs( final Mono<Guild> guild, final Mono<MessageChannel> channel,
                final User caller ) {

            return channel.filter( GuildChannel.class::isInstance )
                    .cast( GuildChannel.class )
                    .flatMap( c -> c.getEffectivePermissions( caller.getId() ) )
                    .map( p -> p.containsAll( permissions ) )
                    .defaultIfEmpty( allowPrivate ); // Not in a guild

        }

    }
    
}
