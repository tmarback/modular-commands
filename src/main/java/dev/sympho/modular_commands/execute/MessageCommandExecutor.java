package dev.sympho.modular_commands.execute;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.checkerframework.checker.optional.qual.Present;

import dev.sympho.modular_commands.api.command.Invocation;
import dev.sympho.modular_commands.api.command.MessageCommand;
import dev.sympho.modular_commands.api.command.handler.MessageInvocationHandler;
import dev.sympho.modular_commands.api.command.handler.MessageResultHandler;
import dev.sympho.modular_commands.api.command.result.CommandResult;
import dev.sympho.modular_commands.api.registry.Registry;
import dev.sympho.modular_commands.impl.context.MessageContextImpl;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

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
     * @param prefixProvider The provider to get prefixes from.
     */
    public MessageCommandExecutor( final GatewayDiscordClient client, final Registry registry,
            PrefixProvider prefixProvider ) {

        super( client, registry, new Builder( prefixProvider ) );

    }

    /**
     * Builder used to construct the processing pipeline.
     *
     * @version 1.0
     * @since 1.0
     */
    private static class Builder extends PipelineBuilder<MessageCreateEvent, 
            MessageCommand, MessageContextImpl, MessageInvocationHandler, MessageResultHandler> {

        /** Provides the prefixes that commands should have. */
        private final PrefixProvider prefixProvider;

        /** 
         * Creates a new instance. 
         *
         * @param prefixProvider Provides the prefixes that commands should have.
         */
        Builder( final PrefixProvider prefixProvider ) {

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
            return event.getMessage().getAuthor().isPresent();
        }
    
        @Override
        protected InvocationValidator<MessageCreateEvent> getValidator() {
            return validator;
        }

        /**
         * Finds the index of the next whitespace character in the given string.
         *
         * @param message The message to look into.
         * @return The index of the first whitespace, or -1 if none were found.
         * @implSpec This method does not consider extended Unicode.
         */
        private static int nextWhitespace( final String message ) {

            for ( int i = 0; i < message.length(); i++ ) {
                if ( Character.isWhitespace( message.charAt( i ) ) ) {
                    return i;
                }
            }
            return -1;
            
        }

        /**
         * Finds the index of the next closing delimiter character in the given message.
         * 
         * <p>A closing delimiter must be followed by either a space or the end of the
         * message. The first character is ignored, as it is assumed to be the opening
         * delimiter.
         *
         * @param message The message to look into.
         * @param delim The delimiter character to look for.
         * @return The index of the delimiter, or -1 if one was not found.
         * @implSpec This method does not consider extended Unicode.
         */
        private static int nextClose( final String message, final Character delim ) {

            int cur = 1;
            while ( cur >= 0 ) {

                cur = message.indexOf( delim, cur );
                if ( cur >= 0 && ( cur == message.length() - 1
                        || Character.isWhitespace( message.charAt( cur + 1 ) ) ) ) {
                    return cur;
                }

            }
            return -1;

        }

        /**
         * Parses the next arg from the message.
         * 
         * <p>Args are delimited by whitespace characters, unless enclosed by quotes (single
         * or double).
         *
         * @param message The message to parse.
         * @return A tuple with the next arg, and the remainder of the message (in that
         *         order).
         * @implSpec This method does not consider extended Unicode.
         */
        private static Tuple2<String, String> nextArg( final String message ) {

            int startIdx = 1;
            int endIdx;
            final int nextStart;

            if ( message.startsWith( "\"" ) ) {
                endIdx = nextClose( message, '"' );
            } else if ( message.startsWith( "'" ) ) {
                endIdx = nextClose( message, '\'' );
            } else {
                startIdx = 0;
                endIdx = nextWhitespace( message );
            }

            if ( endIdx < 0 ) {
                startIdx = 0;
                endIdx = message.length();
                nextStart = message.length();
            } else {
                nextStart = endIdx + 1;
            }

            return Tuples.of( message.substring( startIdx, endIdx ),
                    message.substring( nextStart ).trim() );

        }
    
        @Override
        protected List<String> parse( final MessageCreateEvent event ) {

            String message = event.getMessage().getContent().trim();

            final String prefix = prefixProvider.getPrefix( event.getGuildId().orElse( null ) );
            if ( message.startsWith( prefix ) ) {
                message = message.substring( prefix.length() );
            } else {
                return Collections.emptyList();
            }

            final List<String> args = new LinkedList<>();

            while ( !message.isEmpty() ) {

                final var next = nextArg( message );

                args.add( next.getT1() );
                message = next.getT2();

            }
    
            return args;
    
        }
    
        @Override
        protected MessageContextImpl makeContext( final MessageCreateEvent event,
                final MessageCommand command, final Invocation invocation, 
                final List<String> args ) {
    
            return new MessageContextImpl( event, invocation, command.parameters(), args );
    
        }
    
        @Override
        protected Optional<Snowflake> getGuildId( final MessageCreateEvent event ) {
            return event.getGuildId();
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
