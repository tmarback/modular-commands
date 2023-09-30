package dev.sympho.modular_commands.execute;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.BooleanUtils;

import dev.sympho.bot_utils.access.AccessManager;
import dev.sympho.modular_commands.api.command.Command;
import dev.sympho.modular_commands.api.command.Invocation;
import dev.sympho.modular_commands.api.command.handler.InvocationHandler;
import dev.sympho.modular_commands.api.command.handler.MessageHandlers;
import dev.sympho.modular_commands.api.command.handler.ResultHandler;
import dev.sympho.modular_commands.api.registry.Registry;
import dev.sympho.modular_commands.impl.context.MessageContextImpl;
import dev.sympho.modular_commands.utils.StringSplitter;
import dev.sympho.modular_commands.utils.StringSplitter.Async.Iterator;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.observation.ObservationRegistry;
import reactor.core.publisher.Mono;

/**
 * Executor that receives and handles commands through text messages.
 * 
 * <p>Note that, as command names are restricted to only using lowercase characters,
 * command matching is case insensitive (argument parsing is not affected).
 *
 * @version 1.0
 * @since 1.0
 */
public class MessageCommandExecutor extends BaseCommandExecutor<MessageCreateEvent, 
        MessageContextImpl, MessageHandlers, Iterator> {

    /** Splitter to use for separating arguments in received messages. */
    private final StringSplitter.Async splitter = new StringSplitter.Shell();

    /** Provides the prefixes that commands should have. */
    private final PrefixProvider prefixProvider;

    /** Provides the aliases that should be applied. */
    private final AliasProvider aliases;

    /**
     * Creates a new instance.
     *
     * @param client The client to receive events from.
     * @param registry The registry to use to look up commands.
     * @param accessManager The access manager to use for access checks.
     * @param meters The meter registry to use.
     * @param observations The observation registry to use.
     * @param prefixProvider The provider to get prefixes from.
     * @param aliases Provides the aliases that should be applied.
     */
    public MessageCommandExecutor( final GatewayDiscordClient client, final Registry registry,
            final AccessManager accessManager, 
            final MeterRegistry meters, final ObservationRegistry observations,
            final PrefixProvider prefixProvider, final AliasProvider aliases ) {

        super( client, registry, accessManager, meters, observations );

        this.prefixProvider = prefixProvider;
        this.aliases = aliases;

    }

    @Override
    protected Metrics.Tag.Type tagType() {
        return Metrics.Tag.Type.MESSAGE;
    }

    @Override
    protected Class<MessageCreateEvent> eventType() {
        return MessageCreateEvent.class;
    }

    @Override
    protected Class<MessageHandlers> commandType() {
        return MessageHandlers.class;
    }

    @Override
    protected boolean fullMatch() {
        return false;
    }

    @Override
    protected boolean eventFilter( final MessageCreateEvent event ) {
        final var selfId = event.getClient().getSelfId();
        return event.getMessage().getAuthor()
                .map( User::getId )
                .map( selfId::equals )
                .map( BooleanUtils::negate )
                .orElse( false );
    }

    @Override
    protected Iterator parse( final MessageCreateEvent event ) {

        final String message = event.getMessage().getContent();
        if ( message.isEmpty() || Character.isWhitespace( message.codePointAt( 0 ) ) ) {
            return splitter.emptyIterator();
        }

        final String prefix = prefixProvider.getPrefix( event.getGuildId().orElse( null ) );
        if ( message.startsWith( prefix ) ) {
            final var iter = splitter.iterate( message.substring( prefix.length() ).trim() );
            return aliases.apply( iter );
        } else {
            return splitter.emptyIterator();
        }

    }

    @Override
    protected MessageContextImpl makeContext( final MessageCreateEvent event,
            final Command<? extends MessageHandlers> command, final Invocation invocation, 
            final Iterator args ) {

        return new MessageContextImpl( event, invocation, command, args, accessManager );

    }

    @Override
    protected Optional<Snowflake> getGuildId( final MessageCreateEvent event ) {
        return event.getGuildId();
    }

    @Override
    protected Mono<Guild> getGuild( final MessageCreateEvent event ) {
        return event.getGuild();
    }

    @Override
    protected Snowflake getChannelId( final MessageCreateEvent event ) {
        return event.getMessage().getChannelId();
    }

    @Override
    protected Mono<MessageChannel> getChannel( final MessageCreateEvent event ) {
        return event.getMessage().getChannel();
    }

    @Override
    protected User getCaller( final MessageCreateEvent event ) {

        final var author = event.getMessage().getAuthor();
        if ( author.isPresent() ) {
            return author.get();
        } else {
            throw new IllegalStateException( "Message with no author." );
        }

    }

    @Override
    @SuppressWarnings( "override.return" )
    protected InvocationHandler<? super MessageContextImpl> getInvocationHandler( 
            final MessageHandlers handlers ) {
        return handlers.invocation();
    }

    @Override
    @SuppressWarnings( "override.return" )
    protected List<? extends ResultHandler<? super MessageContextImpl>> getResultHandlers(
            final MessageHandlers handlers ) {
        return handlers.result();
    }
    
}
