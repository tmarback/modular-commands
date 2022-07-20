package dev.sympho.modular_commands.api.permission;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.ApplicationInfo;
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
 * Default group types and composition operations.
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
     */
    public static final Group ADMINS = named(
            hasGuildPermissions( PermissionSet.of( Permission.ADMINISTRATOR ) ),
            "Administrators"
    );

    /** The group that only matches the server owner. */
    public static final Group SERVER_OWNER = named(
            ( guild, channel, user ) -> guild
                    .map( Guild::getOwnerId )
                    .map( user.getId()::equals ),
            "Server Owner"
    );

    /** The group that only matches the bot owner. */
    public static final Group BOT_OWNER = named(
            ( guild, channel, user ) -> user.getClient().getApplicationInfo()
                    .map( ApplicationInfo::getOwnerId )
                    .map( user.getId()::equals ), 
            "Bot Owner" 
    );

    /** Do not instantiate. */
    private Groups() {}

    /* Composition operations */

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

    /**
     * Decorates a group so that membership is always checked in relation to the given guild,
     * rather than the guild where the command was executed in.
     * 
     * <p>In other words, this overrides the {@code guild} parameter provided to the
     * {@link Group#belongs(Mono, Mono, User)} method to the given guild, before delegating
     * to the given group.
     * 
     * <p>This allows for permission checking on commands that always operate on some remote
     * guild, and thus should be checked in relation to that guild.
     *
     * @param group The group.
     * @param guild The guild where group membership should be checked for. The issued value
     *              may change over time.
     * @return The decorated group.
     */
    public static Group remote( final Group group, final Mono<Snowflake> guild ) {

        return new Remote( group, guild );

    }

    /**
     * Decorates a group so that membership is always checked in relation to the given guild,
     * rather than the guild where the command was executed in.
     * 
     * <p>This allows for permission checking on commands that always operate on some remote
     * guild, and thus should be checked in relation to that guild.
     *
     * @param group The group.
     * @param guild The guild where group membership should be checked for. The issued value
     *              may change over time.
     * @return The decorated group.
     */
    public static Group remote( final Group group, final Supplier<Snowflake> guild ) {

        return remote( group, Mono.fromSupplier( guild ) );

    }

    /**
     * Decorates a group so that membership is always checked in relation to the given guild,
     * rather than the guild where the command was executed in.
     * 
     * <p>This allows for permission checking on commands that always operate on some remote
     * guild, and thus should be checked in relation to that guild.
     *
     * @param group The group.
     * @param guild The guild where group membership should be checked for.
     * @return The decorated group.
     */
    public static Group remote( final Group group, final Snowflake guild ) {

        return remote( group, Mono.just( guild ) );

    }

    /**
     * Composes multiple groups into a single group where a user is only a member
     * if they are a member of <b>any</b> of the given groups.
     *
     * @param groups The groups to compose. The issued values may change over time 
     *               (between subscriptions).
     * @return The composed group.
     */
    public static Group any( final Flux<Group> groups ) {

        return ( guild, channel, caller ) -> {

            return groups.flatMap( g -> g.belongs( guild, channel, caller ) )
                    .any( Boolean::booleanValue );

        };

    }

    /**
     * Composes multiple groups into a single group where a user is only a member
     * if they are a member of <b>any</b> of the given groups.
     *
     * @param groups The groups to compose.
     * @return The composed group.
     */
    public static Group any( final Stream<Group> groups ) {
        return any( Flux.fromStream( groups ) );
    }

    /**
     * Composes multiple groups into a single group where a user is only a member
     * if they are a member of <b>any</b> of the given groups.
     *
     * @param groups The groups to compose.
     * @return The composed group.
     */
    public static Group any( final Iterable<Group> groups ) {
        return any( Flux.fromIterable( groups ) );
    }

    /**
     * Composes multiple groups into a single group where a user is only a member
     * if they are a member of <b>any</b> of the given groups.
     *
     * @param groups The groups to compose.
     * @return The composed group.
     */
    public static Group any( final Group... groups ) {
        return any( Flux.fromArray( groups ) );
    }

    /**
     * Composes multiple groups into a single group where a user is only a member
     * if they are a member of <b>all</b> of the given groups.
     *
     * @param groups The groups to compose. The issued values may change over time 
     *               (between subscriptions).
     * @return The composed group.
     */
    public static Group all( final Flux<Group> groups ) {

        return ( guild, channel, caller ) -> {

            return groups.flatMap( g -> g.belongs( guild, channel, caller ) )
                    .all( Boolean::booleanValue );

        };

    }

    /**
     * Composes multiple groups into a single group where a user is only a member
     * if they are a member of <b>all</b> of the given groups.
     *
     * @param groups The groups to compose.
     * @return The composed group.
     */
    public static Group all( final Stream<Group> groups ) {
        return all( Flux.fromStream( groups ) );
    }

    /**
     * Composes multiple groups into a single group where a user is only a member
     * if they are a member of <b>all</b> of the given groups.
     *
     * @param groups The groups to compose.
     * @return The composed group.
     */
    public static Group all( final Iterable<Group> groups ) {
        return all( Flux.fromIterable( groups ) );
    }

    /**
     * Composes multiple groups into a single group where a user is only a member
     * if they are a member of <b>all</b> of the given groups.
     *
     * @param groups The groups to compose.
     * @return The composed group.
     */
    public static Group all( final Group... groups ) {
        return all( Flux.fromArray( groups ) );
    }

    /* Default groups */

    /**
     * Creates a group composed of a single user.
     *
     * @param user The user. The issued value may change over time.
     * @return The group.
     */
    public static Group isUser( final Mono<Snowflake> user ) {

        return ( guild, channel, caller ) -> user.map( caller.getId()::equals );

    }

    /**
     * Creates a group composed of a single user.
     *
     * @param user The user. The issued value may change over time.
     * @return The group.
     */
    public static Group isUser( final Supplier<Snowflake> user ) {

        return isUser( Mono.fromSupplier( user ) );

    }

    /**
     * Creates a group composed of a single user.
     *
     * @param user The user.
     * @return The group.
     */
    public static Group isUser( final Snowflake user ) {

        return isUser( Mono.just( user ) );

    }

    /**
     * Creates a group composed of a set of users.
     *
     * @param users The users that belong to the group. The issued values may vary over
     *              time (between subscriptions).
     * @return The group.
     */
    public static Group inWhitelist( final Flux<Snowflake> users ) {

        return ( guild, channel, caller ) -> users.any( caller.getId()::equals );

    }

    /**
     * Creates a group composed of a set of users.
     *
     * @param users The users that belong to the group. The issued values may vary over
     *              time (between subscriptions).
     * @return The group.
     */
    public static Group inWhitelist( final Mono<? extends Collection<Snowflake>> users ) {

        return ( guild, channel, caller ) -> users.map( u -> u.contains( caller.getId() ) );

    }

    /**
     * Creates a group composed of a set of users.
     *
     * @param users The users that belong to the group. The issued values may vary over
     *              time (between requests).
     * @return The group.
     */
    public static Group inWhitelist( final Supplier<? extends Collection<Snowflake>> users ) {

        return inWhitelist( Mono.fromSupplier( users ) );

    }

    /**
     * Creates a group composed of a set of users.
     *
     * @param users The users that belong to the group.
     * @return The group.
     */
    public static Group inWhitelist( final Collection<Snowflake> users ) {

        final var allowed = Set.copyOf( users );
        return inWhitelist( Mono.just( allowed ) );

    }

    /**
     * Creates a group composed of a set of users.
     *
     * @param users The users that belong to the group.
     * @return The group.
     */
    public static Group inWhitelist( final Snowflake... users ) {
        return inWhitelist( Arrays.asList( users ) );
    }

    /**
     * Creates a group defined as all users that have the given role.
     *
     * @param role The role. The issued value may vary over time.
     * @return The group.
     */
    public static Group hasRole( final Mono<Snowflake> role ) {

        return ( guild, channel, caller ) -> {
            
            final Flux<Snowflake> roles = guild
                    .flatMap( g -> g.getMemberById( caller.getId() ) )
                    .flatMapMany( m -> m.getRoles() )
                    .map( Role::getId );
            
            return role.flatMap( r -> roles.any( r::equals ) );

        };

    }

    /**
     * Creates a group defined as all users that have the given role.
     *
     * @param role The role. The issued value may vary over time.
     * @return The group.
     */
    public static Group hasRole( final Supplier<Snowflake> role ) {

        return hasRole( Mono.fromSupplier( role ) );

    }

    /**
     * Creates a group defined as all users that have the given role.
     *
     * @param role The role.
     * @return The group.
     */
    public static Group hasRole( final Snowflake role ) {

        return hasRole( Mono.just( role ) );

    }

    /**
     * Creates a group defined as all users that have <b>any</b> of the given roles.
     *
     * @param roles The roles. The issued values may vary over time (between subscriptions).
     * @return The group.
     */
    public static Group hasRolesAny( final Mono<? extends Collection<Snowflake>> roles ) {

        return ( guild, channel, caller ) -> {
            
            final Flux<Snowflake> has = guild
                    .flatMap( g -> g.getMemberById( caller.getId() ) )
                    .flatMapMany( m -> m.getRoles() )
                    .map( Role::getId );

            return roles.flatMap( r -> has.any( r::contains ) );

        };

    }

    /**
     * Creates a group defined as all users that have <b>any</b> of the given roles.
     *
     * @param roles The roles. The issued values may vary over time (between subscriptions).
     * @return The group.
     */
    public static Group hasRolesAny( final Flux<Snowflake> roles ) {

        final var allowed = roles.collect( Collectors.toSet() );
        return hasRolesAny( allowed );

    }

    /**
     * Creates a group defined as all users that have <b>any</b> of the given roles.
     *
     * @param roles The roles. The issued values may vary over time.
     * @return The group.
     */
    public static Group hasRolesAny( final Supplier<? extends Collection<Snowflake>> roles ) {

        return hasRolesAny( Mono.fromSupplier( roles ) );

    }

    /**
     * Creates a group defined as all users that have <b>any</b> of the given roles.
     *
     * @param roles The roles.
     * @return The group.
     */
    public static Group hasRolesAny( final Collection<Snowflake> roles ) {

        final var allowed = Set.copyOf( roles );
        return hasRolesAny( Mono.just( allowed ) );

    }

    /**
     * Creates a group defined as all users that have <b>any</b> of the given roles.
     *
     * @param roles The roles.
     * @return The group.
     */
    public static Group hasRolesAny( final Snowflake... roles ) {
        return hasRolesAny( Arrays.asList( roles ) );
    }

    /**
     * Creates a group defined as all users that have <b>all</b> of the given roles.
     *
     * @param roles The roles. The issued values may vary over time (between subscriptions).
     * @return The group.
     */
    public static Group hasRolesAll( final Flux<Snowflake> roles ) {

        return ( guild, channel, caller ) -> {
            
            final Mono<Set<Snowflake>> has = guild
                    .flatMap( g -> g.getMemberById( caller.getId() ) )
                    .flatMapMany( m -> m.getRoles() )
                    .map( Role::getId )
                    .collect( Collectors.toSet() );

            return has.flatMap( r -> roles.all( r::contains ) );

        };

    }

    /**
     * Creates a group defined as all users that have <b>all</b> of the given roles.
     *
     * @param roles The roles. The issued values may vary over time (between subscriptions).
     * @return The group.
     */
    public static Group hasRolesAll( final Mono<? extends Collection<Snowflake>> roles ) {

        return ( guild, channel, caller ) -> {
            
            final Mono<Set<Snowflake>> has = guild
                    .flatMap( g -> g.getMemberById( caller.getId() ) )
                    .flatMapMany( m -> m.getRoles() )
                    .map( Role::getId )
                    .collect( Collectors.toSet() );

            return Mono.zip( has, roles )
                    .map( t -> t.getT1().containsAll( t.getT2() ) );

        };

    }

    /**
     * Creates a group defined as all users that have <b>all</b> of the given roles.
     *
     * @param roles The roles. The issued values may vary over time.
     * @return The group.
     */
    public static Group hasRolesAll( final Supplier<? extends Collection<Snowflake>> roles ) {

        return hasRolesAll( Mono.fromSupplier( roles ) );

    }

    /**
     * Creates a group defined as all users that have <b>all</b> of the given roles.
     *
     * @param roles The roles.
     * @return The group.
     */
    public static Group hasRolesAll( final Collection<Snowflake> roles ) {

        final var required = Set.copyOf( roles );
        return hasRolesAll( Mono.just( required ) );

    }

    /**
     * Creates a group defined as all users that have <b>all</b> of the given roles.
     *
     * @param roles The roles.
     * @return The group.
     */
    public static Group hasRolesAll( final Snowflake... roles ) {
        return hasRolesAll( Arrays.asList( roles ) );
    }

    /**
     * Creates a group defined as all users that have the given permissions in the guild.
     *
     * @param permissions The permissions. The issued value may vary over time.
     * @return The group.
     */
    public static Group hasGuildPermissions( final Mono<PermissionSet> permissions ) {

        return ( guild, channel, caller ) -> guild
                .flatMap( g -> g.getMemberById( caller.getId() ) )
                .flatMap( m -> m.getBasePermissions() )
                .flatMap( p -> permissions.map( p::containsAll ) )
                .defaultIfEmpty( true ); // Not in a guild

    }

    /**
     * Creates a group defined as all users that have the given permissions in the guild.
     *
     * @param permissions The permissions. The issued value may vary over time.
     * @return The group.
     */
    public static Group hasGuildPermissions( final Supplier<PermissionSet> permissions ) {

        return hasGuildPermissions( Mono.fromSupplier( permissions ) );

    }

    /**
     * Creates a group defined as all users that have the given permissions in the guild.
     *
     * @param permissions The permissions.
     * @return The group.
     */
    public static Group hasGuildPermissions( final PermissionSet permissions ) {

        return hasGuildPermissions( Mono.just( permissions ) );

    }

    /**
     * Creates a group defined as all users that have the given permissions in the channel.
     *
     * @param permissions The permissions. The issued value may vary over time.
     * @return The group.
     */
    public static Group hasChannelPermissions( final Mono<PermissionSet> permissions ) {

        return ( guild, channel, caller ) -> channel
                .filter( GuildChannel.class::isInstance )
                .cast( GuildChannel.class )
                .flatMap( c -> c.getEffectivePermissions( caller.getId() ) )
                .flatMap( p -> permissions.map( p::containsAll ) )
                .defaultIfEmpty( true ); // Not in a guild

    }

    /**
     * Creates a group defined as all users that have the given permissions in the channel.
     *
     * @param permissions The permissions. The issued value may vary over time.
     * @return The group.
     */
    public static Group hasChannelPermissions( final Supplier<PermissionSet> permissions ) {

        return hasChannelPermissions( Mono.fromSupplier( permissions ) );

    }

    /**
     * Creates a group defined as all users that have the given permissions in the channel.
     *
     * @param permissions The permissions.
     * @return The group.
     */
    public static Group hasChannelPermissions( final PermissionSet permissions ) {

        return hasChannelPermissions( Mono.just( permissions ) );

    }

    /* Inner classes */

    /**
     * Wrapper for a group that adds a name to it.
     *
     * @param group The wrapped group.
     * @param name The name of the group.
     * @version 1.0
     * @since 1.0
     */
    private record Named( Group group, String name ) implements NamedGroup {

        @Override
        public Mono<Boolean> belongs( final Mono<Guild> guild, 
                final Mono<MessageChannel> channel, final User caller ) {

            return group.belongs( guild, channel, caller );

        }

        @Override
        public String name() {

            return name;

        }

    }

    /**
     * Wrapper for a group that overrides the {@code guild} parameter provided to the
     * {@link Group#belongs(Mono, Mono, User)} method to the given guild, before delegating
     * to the given group.
     *
     * @param group The group.
     * @param remoteGuild The guild where group membership should be checked for. The issued 
     *                    value may change over time.
     * @version 1.0
     * @since 1.0
     */
    private record Remote( Group group, Mono<Snowflake> remoteGuild ) implements Group {

        /**
         * Creates the exception to be thrown when the guild does not exist.
         *
         * @param guild The remote guild ID.
         * @return The exception to throw.
         */
        private static Throwable notFound( final Snowflake guild ) {

            final var message = "Guild %s not found".formatted( guild );
            return new IllegalArgumentException( message );

        }

        @Override
        public Mono<Boolean> belongs( final Mono<Guild> guild, 
                final Mono<MessageChannel> channel, final User caller ) {

            final var client = caller.getClient();
            final var remote = remoteGuild.flatMap( g -> client.getGuildById( g )
                    .switchIfEmpty( Mono.error( () -> notFound( g ) ) )
            );
            return group.belongs( remote, channel, caller );

        }

    }
    
}
