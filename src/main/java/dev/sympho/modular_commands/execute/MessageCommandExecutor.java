package dev.sympho.modular_commands.execute;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.BooleanUtils;
import org.checkerframework.checker.optional.qual.Present;

import dev.sympho.modular_commands.api.command.Invocation;
import dev.sympho.modular_commands.api.command.MessageCommand;
import dev.sympho.modular_commands.api.command.handler.MessageInvocationHandler;
import dev.sympho.modular_commands.api.command.handler.MessageResultHandler;
import dev.sympho.modular_commands.api.command.result.CommandResult;
import dev.sympho.modular_commands.api.registry.Registry;
import dev.sympho.modular_commands.impl.context.MessageContextImpl;
import dev.sympho.modular_commands.utils.StringSplitter;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import reactor.core.publisher.Mono;

/**
 * Executor that receives and handles commands through text messages.
 *
 * @version 1.0
 * @since 1.0
 */
public class MessageCommandExecutor extends CommandExecutor {

    /** The validator used to validate invocations. */
    private static final Validator validator = new Validator();

    /**
     * Creates a new instance.
     *
     * @param client The client to receive events from.
     * @param registry The registry to use to look up commands.
     * @param accessManager The access manager to use for access checks.
     * @param prefixProvider The provider to get prefixes from.
     */
    public MessageCommandExecutor( final GatewayDiscordClient client, final Registry registry,
            final AccessManager accessManager, final PrefixProvider prefixProvider ) {

        super( client, registry, new Builder( accessManager, prefixProvider ) );

    }

    /**
     * Builder used to construct the processing pipeline.
     *
     * @version 1.0
     * @since 1.0
     */
    private static class Builder extends PipelineBuilder<MessageCreateEvent, 
            MessageCommand, MessageContextImpl, MessageInvocationHandler, MessageResultHandler> {

        /** Splitter to use for separating arguments in received messages. */
        private final StringSplitter splitter = new StringSplitter.Shell();

        /** Provides the prefixes that commands should have. */
        private final PrefixProvider prefixProvider;

        /** 
         * Creates a new instance. 
         *
         * @param accessManager The access manager to use for access checks.
         * @param prefixProvider Provides the prefixes that commands should have.
         */
        Builder( final AccessManager accessManager, final PrefixProvider prefixProvider ) {

            super( accessManager );

            this.prefixProvider = prefixProvider;

        }

        @Override
        protected Class<MessageCreateEvent> eventType() {
            return MessageCreateEvent.class;
        }
    
        @Override
        protected Class<MessageCommand> commandType() {
            return MessageCommand.class;
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
        protected InvocationValidator<MessageCreateEvent> getValidator() {
            return validator;
        }
    
        @Override
        protected List<String> parse( final MessageCreateEvent event ) {

            final String message = event.getMessage().getContent();
            if ( message.isEmpty() || Character.isWhitespace( message.codePointAt( 0 ) ) ) {
                return Collections.emptyList();
            }

            final String prefix = prefixProvider.getPrefix( event.getGuildId().orElse( null ) );
            if ( message.startsWith( prefix ) ) {
                return splitter.split( message.substring( prefix.length() ).trim() );
            } else {
                return Collections.emptyList();
            }
    
        }
    
        @Override
        protected MessageContextImpl makeContext( final MessageCreateEvent event,
                final MessageCommand command, final Invocation invocation, 
                final List<String> args ) {

            final var access = accessValidator( event );
            return new MessageContextImpl( event, invocation, command.parameters(), args, access );
    
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
        protected MessageInvocationHandler getInvocationHandler( final MessageCommand command ) {
            return command.invocationHandler();
        }
    
        @Override
        protected Mono<CommandResult> invoke( final MessageInvocationHandler handler,
                final MessageContextImpl context ) throws Exception {
            return handler.handle( context );
        }
    
        @Override
        protected List<? extends MessageResultHandler> getResultHandlers( 
                    final MessageCommand command ) {
            return command.resultHandlers();
        }
    
        @Override
        protected Mono<Boolean> handle( final MessageResultHandler handler, 
                final MessageContextImpl context, final CommandResult result ) {
            return handler.handle( context, result );
        }

    }

    /**
     * The validator used to validate invocations.
     *
     * @version 1.0
     * @since 1.0
     */
    private static class Validator extends InvocationValidator<MessageCreateEvent> {

        /** Creates a new instance. */
        Validator() {}

        @Override
        protected User getCaller( final MessageCreateEvent event ) {

            // Guaranteed to be present due to the event filter.
            @SuppressWarnings( "cast.unsafe" )
            final var author = ( @Present Optional<User> ) event.getMessage().getAuthor();
            return author.get();

        }

        @Override
        protected Mono<MessageChannel> getChannel( final MessageCreateEvent event ) {
            return event.getMessage().getChannel();
        }

        @Override
        protected Mono<Guild> getGuild( final MessageCreateEvent event ) {
            return event.getGuild();
        }

    }
    
}
