package dev.sympho.modular_commands.impl.context;

import java.util.List;

import org.apache.commons.collections4.ListUtils;
import org.checkerframework.checker.nullness.qual.Nullable;

import dev.sympho.modular_commands.api.command.Invocation;
import dev.sympho.modular_commands.api.command.context.MessageCommandContext;
import dev.sympho.modular_commands.api.command.parameter.Parameter;
import dev.sympho.modular_commands.api.command.parameter.StringParameter;
import dev.sympho.modular_commands.api.exception.InvalidArgumentException;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import reactor.core.publisher.Mono;

/**
 * Context object for invocations performed through text messages.
 *
 * @version 1.0
 * @since 1.0
 */
public final class MessageContextImpl extends ContextImpl<String> implements MessageCommandContext {

    /** The event that triggered the invocation. */
    private final MessageCreateEvent event;

    /**
     * Initializes a new context.
     *
     * @param event The event that triggered the invocation.
     * @param invocation The invocation that triggered execution.
     * @param parameters The command parameters.
     * @param args The raw arguments received.
     */
    public MessageContextImpl( final MessageCreateEvent event, final Invocation invocation, 
            final List<Parameter<?>> parameters, final List<String> args ) {

        super( invocation, parameters, adjustArgs( parameters, args ) );

        this.event = event;

    }

    /**
     * Adjusts the args received so that trailing arguments are merged into the final argument,
     * if the final parameter is a string.
     * 
     * <p>This is to allow multi-word arguments without needing to use quotation marks.
     *
     * @param parameters The command parameters.
     * @param args The received raw arguments.
     * @return The adjusted raw arguments.
     */
    private static List<String> adjustArgs( final List<Parameter<?>> parameters, 
            final List<String> args ) {

        if ( args.size() <= parameters.size() ) {
            return args;
        }

        final var last = parameters.size() - 1;
        if ( parameters.get( last ) instanceof StringParameter ) {
            final List<String> head = args.subList( 0, last );
            final List<String> tail = args.subList( last, args.size() );
            final String expandedLast = String.join( " ", tail );
            return ListUtils.union( head, List.of( expandedLast ) );
        }

        return args;

    }

    @Override
    protected Mono<Object> parseArgument( final Parameter<?> parameter, final String raw ) 
            throws InvalidArgumentException {

        return parameter.parse( this, raw ).map( a -> a );

    }

    @Override
    public User getCaller() {

        final var author = event.getMessage().getAuthor();
        if ( author.isPresent() ) {
            return author.get();
        } else {
            throw new IllegalStateException( "Message with no author." );
        }

    }

    @Override
    public @Nullable Member getCallerMember() {

        return null;

    }

    @Override
    public Mono<MessageChannel> getChannel() {

        return event.getMessage().getChannel();

    }

    @Override
    public Snowflake getChannelId() {

        return event.getMessage().getChannelId();

    }

    @Override
    public Mono<Guild> getGuild() {

        return event.getGuild();

    }

    @Override
    public @Nullable Snowflake getGuildId() {

        return event.getGuildId().orElse( null );

    }

    @Override
    public MessageCreateEvent getMessageEvent() {

        return event;

    }
    
}
