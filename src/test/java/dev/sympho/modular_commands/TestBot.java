package dev.sympho.modular_commands;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.io.input.AutoCloseInputStream;
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
import dev.sympho.modular_commands.utils.parse.ParseUtils;
import discord4j.core.DiscordClient;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.discordjson.possible.PossibleModule;
import discord4j.gateway.intent.IntentSet;
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
import reactor.core.publisher.Hooks;
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

        return Command.builder()
                .name( "ping" )
                .displayName( "Ping Command" )
                .description( "Responds with pong" )
                .handlers( Handlers.text( c -> sendMessage( c, "Pong!" ) ) )
                .id( "ping" )
                .build();

    }

    /**
     * Creates a parrot command.
     *
     * @return The command.
     */
    private static Command<?> parrotCommand() {

        final var param = Parameter.<String>builder()
                .name( "message" )
                .description( "The message to repeat" )
                .required( true )
                .parser( Parsers.text() )
                .build();

        return Command.builder()
                .name( "parrot" )
                .displayName( "Parrot Command" )
                .description( "Responds with what you said" )
                .addParameters( param )
                .handlers( Handlers.text( c -> {
                    final String message = c.requireArgument( param, String.class );
                    final String response = String.format( "You said: %s", message );
                    return c.reply( response ).thenReturn( Results.ok() );
                } ) )
                .id( "parrot" )
                .build();

    }

    /**
     * Creates a tweet command.
     *
     * @return The command.
     */
    private static Command<?> tweetCommand() {

        final var param = Parameter.<Channel>builder()
                .name( "channel" )
                .description( "The channel to send to" )
                .required( true )
                .parser( Parsers.channel( Channel.class ) )
                .build();

        return Command.builder()
                .name( "tweet" )
                .displayName( "Tweet Command" )
                .description( "Says something in a channel" )
                .addParameters( param )
                .handlers( Handlers.text( ctx -> {
                    final Channel c = ctx.requireArgument( param, Channel.class );
                    if ( c instanceof MessageChannel ch ) {
                        return ch.createMessage( "Tweet" )
                                .then( Results.ackMono( ReactionEmoji.unicode( "💯" ), "Sent." ) );
                    } else {
                        return Results.failMono();
                    }

                } ) )
                .id( "tweet" )
                .build();

    }

    /**
     * Creates a list command.
     *
     * @return The command.
     */
    private static Command<?> listCommand() {

        final var param = Parameter.<List<Long>>builder()
                .name( "list" )
                .description( "The list values" )
                .required( true )
                .parser( ParseUtils.integers() )
                .build();

        return Command.builder()
                .name( "list" )
                .displayName( "List Command" )
                .description( "Parses a list" )
                .addParameters( param )
                .handlers( Handlers.text( ctx -> {
                    final List<Long> l = ctx.requireArgument( param );
                    return ctx.reply( "Received items: " + l )
                            .then( Results.okMono() );

                } ) )
                .id( "list" )
                .build();

    }

    /**
     * Creates an admin command.
     *
     * @return The command.
     */
    private static Command<?> adminCommand() {

        return Command.builder()
                .name( "admin" )
                .displayName( "Admin Command" )
                .description( "Only works when used by admins" )
                .skipGroupCheckOnInteraction( false )
                .requiredGroup( Groups.ADMINS )
                .handlers( Handlers.text( ctx -> Results.successMono( "You are an admin!" ) ) )
                .id( "admin" )
                .build();

    }

    /**
     * Creates a mod command.
     *
     * @return The command.
     */
    private static Command<?> modCommand() {

        return Command.builder()
                .name( "mod" )
                .displayName( "Mod Command" )
                .description( "Only works when used by mods" )
                .skipGroupCheckOnInteraction( false )
                .requiredGroup( Groups.hasGuildPermissions( PermissionSet.of( 
                        Permission.MANAGE_MESSAGES 
                ) ) )
                .handlers( Handlers.text( ctx -> Results.successMono( "You are a mod!" ) ) )
                .id( "mod" )
                .build();

    }

    /**
     * Creates a server owner command.
     *
     * @return The command.
     */
    private static Command<?> serverOwnerCommand() {

        return Command.builder()
                .name( "server-owner" )
                .displayName( "Server Owner Command" )
                .description( "Only works when used by server owners" )
                .skipGroupCheckOnInteraction( false )
                .requiredGroup( Groups.SERVER_OWNER )
                .handlers( Handlers.text( 
                        ctx -> Results.successMono( "You are the server owner!" )
                ) )
                .id( "server-owner" )
                .build();

    }

    /**
     * Creates a bot owner command.
     *
     * @return The command.
     */
    private static Command<?> botOwnerCommand() {

        return Command.builder()
                .name( "bot-owner" )
                .displayName( "Bot Owner Command" )
                .description( "Only works when used by the bot owner" )
                .skipGroupCheckOnInteraction( false )
                .requiredGroup( Groups.BOT_OWNER )
                .handlers( Handlers.text( 
                        ctx -> Results.successMono( "You are the bot owner!" ) 
                ) )
                .id( "bot-owner" )
                .build();

    }

    /**
     * Creates a text file command.
     *
     * @return The command.
     */
    private static Command<?> fileTextCommand() {

        final var param = Parameter.<String>builder()
                .name( "file" )
                .description( "The file to read" )
                .required( true )
                .parser( Parsers.textFile( SizeUtils.kilo( 1 ) + 100 ) )
                .build();

        return Command.builder()
                .name( "file-text" )
                .displayName( "Text File Command" )
                .description( "Reads a text file" )
                .addParameters( param )
                .handlers( Handlers.text( ctx -> {

                    final var content = ctx.requireArgument( param, String.class );
                    return Results.successMono( content );

                } ) )
                .id( "file-text" )
                .build();

    }

    /**
     * Creates a command to show member data.
     *
     * @return The command.
     */
    private static Command<?> memberCommand() {

        final var param = Parameter.<Member>builder()
                .name( "target" )
                .description( "The target user" )
                .required( true )
                .parser( ParseUtils.member() )
                .build();

        return Command.builder()
                .name( "member" )
                .displayName( "Get member info" )
                .description( "Show info about a server member" )
                .addParameters( param )
                .handlers( Handlers.text( ctx -> {

                    final var target = ctx.requireArgument( param );
                    final var embed = EmbedCreateSpec.builder()
                            .author( target.getUsername(), null, target.getAvatarUrl() )
                            .addField( "Nickname", 
                                    target.getNickname().orElse( "N/A" ), 
                                    true 
                            )
                            .addField( "Member since", 
                                    target.getJoinTime().orElse( Instant.now() ).toString(), 
                                    true 
                            )
                            .build();

                    return ctx.reply( embed ).thenReturn( Results.ok() );

                } ) )
                .id( "member" )
                .build();

    }

    /**
     * Creates a command to dump message data.
     *
     * @return The command.
     */
    private static Command<?> dumpMessageCommand() {

        final var param = Parameter.<Message>builder()
                .name( "target" )
                .description( "The target message" )
                .required( true )
                .parser( Parsers.message() )
                .build();

        return Command.builder()
                .name( "dump-message" )
                .displayName( "Dump message" )
                .description( "Dumps message content as JSON" )
                .addParameters( param )
                .handlers( Handlers.text( ctx -> {

                    final var message = ctx.requireArgument( param );

                    final var json = new ObjectMapper()
                            .registerModule( new PossibleModule() )
                            .writerWithDefaultPrettyPrinter()
                            .writeValueAsString( message.getData() );

                    final var stream = AutoCloseInputStream.builder()
                            .setCharSequence( json )
                            .setCharset( StandardCharsets.UTF_8 )
                            .get();

                    final var spec = MessageCreateSpec.builder()
                            .addFile( "content.json", stream )
                            .build();

                    return ctx.reply( spec ).thenReturn( Results.ok() );

                } ) )
                .id( "dump-message" )
                .build();

    }

    /**
     * Creates a command for showing meter values.
     *
     * @param registry The meter registry to use.
     * @return The command.
     */
    private static Command<?> metersCommand( final MeterRegistry registry ) {

        final var counter = registry.counter( "meter.shows", "test", "true", "prod", "false" );

        return Command.builder()
                .name( "meters" )
                .displayName( "Show meters" )
                .description( "Shows current meter values" )
                .handlers( Handlers.text( ctx -> {

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
                .id( "meters" )
                .build();
        
    }

    /**
     * Main runner.
     *
     * @param args Command line arguments.
     */
    public static void main( final String[] args ) {

        // Enable context propagation so tracing works
        Hooks.enableAutomaticContextPropagation();

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
                LOGGER.trace( "Tags: {}", context.getAllKeyValues() );
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

                memberCommand(),

                dumpMessageCommand(),

                metersCommand( meters )
        );

        final AliasProvider aliases = AliasProvider.of(
                Map.entry( Invocation.of( "pong" ), Invocation.of( "ping" ) )
        );
        
        DiscordClient.create( token )
            .gateway()
            .setEnabledIntents( IntentSet.all() )
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
