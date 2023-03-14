package dev.sympho.modular_commands;

import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.sympho.modular_commands.api.command.Command;
import dev.sympho.modular_commands.api.command.Invocation;
import dev.sympho.modular_commands.api.command.context.CommandContext;
import dev.sympho.modular_commands.api.command.handler.Handlers;
import dev.sympho.modular_commands.api.command.parameter.Parameter;
import dev.sympho.modular_commands.api.command.parameter.parse.Parsers;
import dev.sympho.modular_commands.api.command.result.CommandResult;
import dev.sympho.modular_commands.api.command.result.Results;
import dev.sympho.modular_commands.api.permission.Groups;
import dev.sympho.modular_commands.api.registry.Registry;
import dev.sympho.modular_commands.execute.AccessManager;
import dev.sympho.modular_commands.execute.AliasProvider;
import dev.sympho.modular_commands.execute.CommandExecutor;
import dev.sympho.modular_commands.execute.MessageCommandExecutor;
import dev.sympho.modular_commands.execute.PrefixProvider;
import dev.sympho.modular_commands.execute.StaticPrefix;
import dev.sympho.modular_commands.utils.Registries;
import dev.sympho.modular_commands.utils.SizeUtils;
import dev.sympho.modular_commands.utils.builder.CommandBuilder;
import dev.sympho.modular_commands.utils.builder.ParameterBuilder;
import dev.sympho.modular_commands.utils.parse.ParseUtils;
import discord4j.core.DiscordClient;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;
import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.observation.Observation;
import io.micrometer.observation.Observation.Context;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.observation.ObservationRegistry;
import reactor.core.publisher.Flux;
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
        final var replies = context.replies();
        return replies.add( message )
                    .then( replies.add().withPrivately( true ).withContent( "Test1" ) )
                    .then( replies.add().withPrivately( false ).withContent( "Test2" ) )
                    .then( replies.add().withPrivately( true ).withContent( "Test3" ) )
                    .then( replies.add().withPrivately( false ).withContent( "Test4" ) )
                    .thenReturn( Results.ok() );

    }

    /**
     * Creates a ping command.
     *
     * @return The command.
     */
    private static Command<?> pingCommand() {

        return new CommandBuilder<>()
                .withName( "ping" )
                .withDisplayName( "Ping Command" )
                .withDescription( "Responds with pong" )
                .withHandlers( Handlers.text( c -> sendMessage( c, "Pong!" ) ) )
                .build( "ping" );

    }

    /**
     * Creates a parrot command.
     *
     * @return The command.
     */
    private static Command<?> parrotCommand() {

        final var param = Parameter.<String>builder()
                .withName( "message" )
                .withDescription( "The message to repeat" )
                .withRequired( true )
                .withParser( Parsers.text() )
                .build();

        return new CommandBuilder<>()
                .withName( "parrot" )
                .withDisplayName( "Parrot Command" )
                .withDescription( "Responds with what you said" )
                .addParameter( param )
                .withHandlers( Handlers.text( c -> {
                    final String message = c.requireArgument( param, String.class );
                    final String response = String.format( "You said: %s", message );
                    return c.reply( response ).thenReturn( Results.ok() );
                } ) )
                .build( "parrot" );

    }

    /**
     * Creates a tweet command.
     *
     * @return The command.
     */
    private static Command<?> tweetCommand() {

        final var param = Parameter.<Channel>builder()
                .withName( "channel" )
                .withDescription( "The channel to send to" )
                .withRequired( true )
                .withParser( Parsers.channel( Channel.class ) )
                .build();

        return new CommandBuilder<>()
                .withName( "tweet" )
                .withDisplayName( "Tweet Command" )
                .withDescription( "Says something in a channel" )
                .addParameter( param )
                .withHandlers( Handlers.text( ctx -> {
                    final Channel c = ctx.requireArgument( param, Channel.class );
                    if ( c instanceof MessageChannel ch ) {
                        return ch.createMessage( "Tweet" )
                                .then( Results.successMono( "Sent." ) );
                    } else {
                        return Results.failMono();
                    }

                } ) )
                .build( "tweet" );

    }

    /**
     * Creates a list command.
     *
     * @return The command.
     */
    private static Command<?> listCommand() {

        final var param = Parameter.<List<Long>>builder()
                .withName( "list" )
                .withDescription( "The list values" )
                .withRequired( true )
                .withParser( ParseUtils.integers() )
                .build();

        return new CommandBuilder<>()
                .withName( "list" )
                .withDisplayName( "List Command" )
                .withDescription( "Parses a list" )
                .addParameter( param )
                .withHandlers( Handlers.text( ctx -> {
                    final List<Long> l = ctx.requireArgument( param );
                    return ctx.reply( "Received items: " + l )
                            .then( Results.okMono() );

                } ) )
                .build( "list" );

    }

    /**
     * Creates an admin command.
     *
     * @return The command.
     */
    private static Command<?> adminCommand() {

        return new CommandBuilder<>()
                .withName( "admin" )
                .withDisplayName( "Admin Command" )
                .withDescription( "Only works when used by admins" )
                .setSkipGroupCheckOnInteraction( false )
                .requireGroup( Groups.ADMINS )
                .withHandlers( Handlers.text( ctx -> Results.successMono( "You are an admin!" ) ) )
                .build( "admin" );

    }

    /**
     * Creates a mod command.
     *
     * @return The command.
     */
    private static Command<?> modCommand() {

        return new CommandBuilder<>()
                .withName( "mod" )
                .withDisplayName( "Mod Command" )
                .withDescription( "Only works when used by mods" )
                .setSkipGroupCheckOnInteraction( false )
                .requireGroup( Groups.hasGuildPermissions( PermissionSet.of( 
                        Permission.MANAGE_MESSAGES 
                ) ) )
                .withHandlers( Handlers.text( ctx -> Results.successMono( "You are a mod!" ) ) )
                .build( "mod" );

    }

    /**
     * Creates a server owner command.
     *
     * @return The command.
     */
    private static Command<?> serverOwnerCommand() {

        return new CommandBuilder<>()
                .withName( "server-owner" )
                .withDisplayName( "Server Owner Command" )
                .withDescription( "Only works when used by server owners" )
                .setSkipGroupCheckOnInteraction( false )
                .requireGroup( Groups.SERVER_OWNER )
                .withHandlers( Handlers.text( 
                        ctx -> Results.successMono( "You are the server owner!" )
                ) )
                .build( "server-owner" );

    }

    /**
     * Creates a bot owner command.
     *
     * @return The command.
     */
    private static Command<?> botOwnerCommand() {

        return new CommandBuilder<>()
                .withName( "bot-owner" )
                .withDisplayName( "Bot Owner Command" )
                .withDescription( "Only works when used by the bot owner" )
                .setSkipGroupCheckOnInteraction( false )
                .requireGroup( Groups.BOT_OWNER )
                .withHandlers( Handlers.text( 
                        ctx -> Results.successMono( "You are the bot owner!" ) 
                ) )
                .build( "bot-owner" );

    }

    /**
     * Creates a text file command.
     *
     * @return The command.
     */
    private static Command<?> fileTextCommand() {

        final var param = new ParameterBuilder<String>()
                .withName( "file" )
                .withDescription( "The file to read" )
                .withRequired( true )
                .withParser( Parsers.textFile( SizeUtils.kilo( 1 ) + 100 ) )
                .build();

        return new CommandBuilder<>()
                .withName( "file-text" )
                .withDisplayName( "Text File Command" )
                .withDescription( "Reads a text file" )
                .addParameter( param )
                .withHandlers( Handlers.text( ctx -> {

                    final var content = ctx.requireArgument( param, String.class );
                    return Results.successMono( content );

                } ) )
                .build( "file-text" );

    }

    /**
     * Creates a command for showing meter values.
     *
     * @param registry The meter registry to use.
     * @return The command.
     */
    private static Command<?> metersCommand( final MeterRegistry registry ) {

        final var counter = registry.counter( "meter.shows", "test", "true", "prod", "false" );

        return CommandBuilder.text()
                .withName( "meters" )
                .withDisplayName( "Show meters" )
                .withDescription( "Shows current meter values" )
                .withHandlers( Handlers.text( ctx -> {

                    counter.increment();

                    if ( registry.getMeters().isEmpty() ) {
                        return Results.failureMono( "No meters!" );
                    }

                    return Flux.fromIterable( registry.getMeters() )
                            .map( meter -> {

                                final var name = meter.getId().getName();
                                LOGGER.debug( "Found meter {}", name );

                                final var tags = meter.getId().getTags().stream()
                                        .map( tag -> "`%s`: `%s`".formatted( 
                                                tag.getKey(), tag.getValue() 
                                        ) )
                                        .toList();

                                final var measurements = StreamSupport.stream( 
                                        meter.measure().spliterator(), false )
                                        .map( Measurement::toString )
                                        .toList();

                                return """
                                        Meter: `%s`

                                        Tags:
                                        %s

                                        Measurements:
                                        %s
                                        """.formatted(
                                                name,
                                                String.join( "\n", tags ),
                                                String.join( "\n", measurements )
                                        );

                            } )
                            .flatMap( ctx::reply )
                            .then( Results.okMono() );

                } ) )
                .build( "meters" );
        
    }

    /**
     * Main runner.
     *
     * @param args Command line arguments.
     */
    public static void main( final String[] args ) {

        final String token = args[0];

        final MeterRegistry meters = new SimpleMeterRegistry();
        final ObservationRegistry observations = ObservationRegistry.create();

        final var observationHandler = new ObservationHandler<Observation.Context>() {

            @Override
            public void onStart( final Observation.Context context ) {
                LOGGER.trace( "OBSERVATION START: {}", context.getName() );
                LOGGER.trace( "Tags: {}", context.getAllKeyValues() );
            }

            @Override
            public void onError( final Observation.Context context ) {
                LOGGER.error( "OBSERVATION ERROR: " + context.getName(), context.getError() );
            }

            @Override
            public void onEvent( final Observation.Event event, 
                    final Observation.Context context ) {
                LOGGER.trace( "OBSERVATION EVENT: {} - {}", context.getName(), event );
            }

            @Override
            public void onStop( final Observation.Context context ) {
                LOGGER.trace( "OBSERVATION STOP: {}", context.getName() );
            }

            @Override
            public boolean supportsContext( final Context context ) {

                return true;

            }

        };
        observations.observationConfig().observationHandler( observationHandler );

        final PrefixProvider prefix = new StaticPrefix( "t!" );
        final Registry registry = Registries.simpleRegistry();

        registry.registerCommands( 
                pingCommand(),
                parrotCommand(),
                tweetCommand(),
                listCommand(),

                adminCommand(),
                modCommand(),
                serverOwnerCommand(),
                botOwnerCommand(),

                fileTextCommand(),

                metersCommand( meters )
        );

        final AliasProvider aliases = AliasProvider.of(
                Map.entry( Invocation.of( "pong" ), Invocation.of( "ping" ) )
        );
        
        DiscordClient.create( token )
            .withGateway( client -> {

                final List<CommandExecutor> executors = List.of(
                    new MessageCommandExecutor( 
                            client, registry, AccessManager.basic(), 
                            meters, observations, 
                            prefix, aliases 
                    )
                );

                executors.forEach( CommandExecutor::start );
                
                return Mono.empty();
            } ).block();

    }
    
}
