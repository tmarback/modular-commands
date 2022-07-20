package dev.sympho.modular_commands;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.sympho.modular_commands.api.command.Command;
import dev.sympho.modular_commands.api.command.context.CommandContext;
import dev.sympho.modular_commands.api.command.result.CommandResult;
import dev.sympho.modular_commands.api.command.result.Results;
import dev.sympho.modular_commands.api.permission.Groups;
import dev.sympho.modular_commands.api.registry.Registry;
import dev.sympho.modular_commands.execute.AccessManager;
import dev.sympho.modular_commands.execute.CommandExecutor;
import dev.sympho.modular_commands.execute.MessageCommandExecutor;
import dev.sympho.modular_commands.execute.PrefixProvider;
import dev.sympho.modular_commands.execute.StaticPrefix;
import dev.sympho.modular_commands.utils.Registries;
import dev.sympho.modular_commands.utils.builder.command.TextCommandBuilder;
import dev.sympho.modular_commands.utils.builder.parameter.ChannelParameterBuilder;
import dev.sympho.modular_commands.utils.builder.parameter.StringParameterBuilder;
import discord4j.core.DiscordClient;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;
import reactor.core.publisher.Mono;

/**
 * A simple bot to test commands manually.
 *
 * @version 1.0
 * @since 1.0
 */
public class TestBot {

    /** The logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger( TestBot.class );

    /**
     * Sends a message on the channel that the command was received.
     *
     * @param context The command context.
     * @param message The message to send.
     * @return A success result once the message is sent.
     */
    private static Mono<CommandResult> sendMessage( final CommandContext context,
            final String message ) {

        LOGGER.info( "Sending message: {}", message );
        return context.reply( message ).flatMap( m -> Mono.just( m )
                    .then( context.replyManager().setPrivate( true ).add( "Test1" ) )
                    .then( context.replyManager().setPrivate( false ).add( "Test2" ) )
                    .then( context.replyManager().setPrivate( true ).add( "Test3" ) )
                    .then( context.replyManager().setPrivate( false ).add( "Test4" ) )
        ).thenReturn( Results.ok() );

    }

    /**
     * Creates a ping command.
     *
     * @return The command.
     */
    private static Command pingCommand() {

        return new TextCommandBuilder()
                .withName( "ping" )
                .withDisplayName( "Ping Command" )
                .withDescription( "Responds with pong" )
                .withInvocationHandler( c -> sendMessage( c, "Pong!" ) )
                .build();

    }

    /**
     * Creates a parrot command.
     *
     * @return The command.
     */
    private static Command parrotCommand() {

        return new TextCommandBuilder()
                .withName( "parrot" )
                .withDisplayName( "Parrot Command" )
                .withDescription( "Responds with what you said" )
                .addParameter( new StringParameterBuilder()
                    .withName( "message" )
                    .withDescription( "The message to repeat" )
                    .withRequired( true )
                    .build()
                )
                .withInvocationHandler( c -> {
                    final String message = c.requireArgument( "message", String.class );
                    final String response = String.format( "You said: %s", message );
                    return sendMessage( c, response );
                } )
                .build();

    }

    /**
     * Creates a tweet command.
     *
     * @return The command.
     */
    private static Command tweetCommand() {

        return new TextCommandBuilder()
                .withName( "tweet" )
                .withDisplayName( "Tweet Command" )
                .withDescription( "Says something in a channel" )
                .addParameter( new ChannelParameterBuilder()
                    .withName( "channel" )
                    .withDescription( "The channel to send to" )
                    .withRequired( true )
                    .build()
                )
                .withInvocationHandler( ctx -> {
                    final Channel c = ctx.requireArgument( "channel", Channel.class );
                    if ( c instanceof MessageChannel ch ) {
                        return ch.createMessage( "Tweet" )
                                .then( Results.successMono( "Sent." ) );
                    } else {
                        return Results.failMono();
                    }

                } )
                .build();

    }

    /**
     * Creates an admin command.
     *
     * @return The command.
     */
    private static Command adminCommand() {

        return new TextCommandBuilder()
                .withName( "admin" )
                .withDisplayName( "Admin Command" )
                .withDescription( "Only works when used by admins" )
                .setSkipGroupCheckOnInteraction( false )
                .requireGroup( Groups.ADMINS )
                .withInvocationHandler( ctx -> Results.successMono( "You are an admin!" ) )
                .build();

    }

    /**
     * Creates a mod command.
     *
     * @return The command.
     */
    private static Command modCommand() {

        return new TextCommandBuilder()
                .withName( "mod" )
                .withDisplayName( "Mod Command" )
                .withDescription( "Only works when used by mods" )
                .setSkipGroupCheckOnInteraction( false )
                .requireGroup( Groups.hasGuildPermissions( PermissionSet.of( 
                        Permission.MANAGE_MESSAGES 
                ) ) )
                .withInvocationHandler( ctx -> Results.successMono( "You are a mod!" ) )
                .build();

    }

    /**
     * Creates a server owner command.
     *
     * @return The command.
     */
    private static Command serverOwnerCommand() {

        return new TextCommandBuilder()
                .withName( "server-owner" )
                .withDisplayName( "Server Owner Command" )
                .withDescription( "Only works when used by server owners" )
                .setSkipGroupCheckOnInteraction( false )
                .requireGroup( Groups.SERVER_OWNER )
                .withInvocationHandler( ctx -> Results.successMono( "You are the server owner!" ) )
                .build();

    }

    /**
     * Creates a bot owner command.
     *
     * @return The command.
     */
    private static Command botOwnerCommand() {

        return new TextCommandBuilder()
                .withName( "bot-owner" )
                .withDisplayName( "Bot Owner Command" )
                .withDescription( "Only works when used by the bot owner" )
                .setSkipGroupCheckOnInteraction( false )
                .requireGroup( Groups.BOT_OWNER )
                .withInvocationHandler( ctx -> Results.successMono( "You are the bot owner!" ) )
                .build();

    }

    /**
     * Main runner.
     *
     * @param args Command line arguments.
     */
    public static void main( final String[] args ) {

        final String token = args[0];

        final PrefixProvider prefix = new StaticPrefix( "t!" );
        final Registry registry = Registries.simpleRegistry();

        registry.registerCommand( "ping", pingCommand() );
        registry.registerCommand( "parrot", parrotCommand() );
        registry.registerCommand( "tweet", tweetCommand() );

        registry.registerCommand( "admin", adminCommand() );
        registry.registerCommand( "mod", modCommand() );
        registry.registerCommand( "server-owner", serverOwnerCommand() );
        registry.registerCommand( "bot-owner", botOwnerCommand() );
        
        DiscordClient.create( token )
            .withGateway( client -> {

                final List<CommandExecutor> executors = List.of(
                    new MessageCommandExecutor( client, registry, AccessManager.basic(), prefix )
                );

                executors.forEach( CommandExecutor::start );
                
                return Mono.empty();
            } ).block();

    }
    
}
