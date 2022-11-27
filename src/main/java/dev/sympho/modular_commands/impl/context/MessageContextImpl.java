package dev.sympho.modular_commands.impl.context;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.Streams;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.Invocation;
import dev.sympho.modular_commands.api.command.ReplyManager;
import dev.sympho.modular_commands.api.command.context.MessageCommandContext;
import dev.sympho.modular_commands.api.command.parameter.Parameter;
import dev.sympho.modular_commands.api.command.parameter.parse.AttachmentParser;
import dev.sympho.modular_commands.api.command.parameter.parse.InputParser;
import dev.sympho.modular_commands.api.command.parameter.parse.InvalidArgumentException;
import dev.sympho.modular_commands.api.command.parameter.parse.SnowflakeParser;
import dev.sympho.modular_commands.api.command.parameter.parse.StringParser;
import dev.sympho.modular_commands.api.command.result.CommandFailureArgumentExtra;
import dev.sympho.modular_commands.api.exception.ResultException;
import dev.sympho.modular_commands.api.permission.AccessValidator;
import dev.sympho.modular_commands.utils.StringSplitter.Async.Iterator;
import dev.sympho.modular_commands.utils.parse.RawParser;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.MessageChannel;
import reactor.core.publisher.Flux;
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

    /** The received inline arguments. */
    private final Iterator arguments;

    /** The arguments received as text. */
    private @MonotonicNonNull Map<String, String> inputArgs;

    /** The arguments received as attachments. */
    private @MonotonicNonNull Map<String, Attachment> attachmentArgs;

    /**
     * Initializes a new context.
     *
     * @param event      The event that triggered the invocation.
     * @param invocation The invocation that triggered execution.
     * @param parameters The command parameters.
     * @param args       The raw arguments received.
     * @param access     The validator to use for access checks.
     * @throws ResultException if there is a mismatch between parameters and
     *                         arguments.
     */
    public MessageContextImpl( final MessageCreateEvent event, final Invocation invocation,
            final List<Parameter<?>> parameters, final Iterator args,
            final AccessValidator access ) throws ResultException {

        super( invocation, parameters, access );

        this.event = event;
        this.arguments = args;

    }

    /**
     * Assigns arguments to the corresponding parameters.
     *
     * @param <T>        The raw argument type.
     * @param parameters The parameters.
     * @param arguments  The arguments to assign.
     * @param toString   The function to use to convert an argument into an
     *                   identifier for error messages.
     * @return The arguments, keyed by the corresponding parameter name.
     * @throws ResultException if there is a mismatch.
     */
    @SideEffectFree
    private static <T> Map<String, T> assign( final List<? extends Parameter<?>> parameters,
            final List<T> arguments, final Function<T, String> toString ) throws ResultException {

        final var merged = Streams.zip( 
                    parameters.stream().map( Parameter::name ),
                    arguments.stream(), 
                    Map::entry 
                )
                .collect( Collectors.toMap( 
                    Entry::getKey, 
                    Entry::getValue 
                ) );

        if ( merged.size() < arguments.size() ) {

            final var extra = arguments.stream().skip( merged.size() ).map( toString ).toList();
            throw new ResultException( new CommandFailureArgumentExtra( extra ) );

        }

        return merged;

    }

    /**
     * Adjusts the args received so that trailing arguments are merged into the
     * final argument, if the final parameter is a string.
     * 
     * <p>This is to allow multi-word arguments without needing to use quotation marks.
     *
     * @param parameters The command parameters.
     * @param args       The received raw arguments.
     * @return The adjusted raw arguments.
     */
    @SideEffectFree
    private static Flux<String> adjustArgs( final List<Parameter<?>> parameters,
            final Iterator args ) {

        if ( parameters.isEmpty() ) {
            return Flux.empty();
        }

        final var lastIdx = parameters.size() - 1;
        final var last = parameters.get( lastIdx ).parser();
        final var merge = last instanceof StringParser<?> p && p.allowMerge();
        final var index = new AtomicInteger( 0 );

        return Flux.generate( sink -> {

            if ( !args.hasNext() ) {
                sink.complete();
            } else if ( merge && index.getAndIncrement() == lastIdx ) {
                sink.next( args.remainder() );
                sink.complete();
            } else {
                sink.next( args.next() );
            }

        } );

    }

    @Override
    protected Mono<Void> initArgs() {

        return Mono.defer( () -> {

            final var inputParams = parameters.stream()
                    .filter( p -> p.parser() instanceof InputParser<?, ?> ).toList();
            final var attachmentParams = parameters.stream()
                    .filter( p -> p.parser() instanceof AttachmentParser<?> ).toList();

            final var attachments = event.getMessage().getAttachments();
            this.attachmentArgs = assign( attachmentParams, attachments, Attachment::getFilename );

            return adjustArgs( inputParams, arguments )
                    .collectList()
                    .map( args -> assign( inputParams, args, Function.identity() ) )
                    .doOnNext( args -> { 
                        this.inputArgs = args; 
                    } )
                    .then();

        } );

    }

    @Override
    protected Mono<ReplyManager> makeReplyManager() {

        final var message = getEvent().getMessage();
        final var caller = getCaller();
        return Mono.zip( getChannel(), caller.getPrivateChannel() ).map( t -> {

            final var publicChannel = t.getT1();
            final var privateChannel = t.getT2();
            return new MessageReplyManager( message, publicChannel, privateChannel );

        } );

    }

    /**
     * Retrieves the input arguments.
     *
     * @return The input arguments, or an empty map if not initialized.
     */
    private Map<String, String> getInputArgs() {
        return Objects.requireNonNullElse( inputArgs, Collections.emptyMap() );
    }

    /**
     * Retrieves the attachment arguments.
     *
     * @return The attachment arguments, or an empty map if not initialized.
     */
    private Map<String, Attachment> getAttachmentArgs() {
        return Objects.requireNonNullElse( attachmentArgs, Collections.emptyMap() );
    }

    /**
     * Retrieves an input argument and performs initial parsing on it.
     *
     * @param <P> The raw value type.
     * @param name The parameter name.
     * @param parser The parser to use.
     * @return The pre-parsed raw argument. Will be empty if the argument
     *         is missing.
     */
    private <P extends @NonNull Object> Mono<P> parse( final String name,
            final RawParser<P> parser ) {

        final var raw = getInputArgs().get( name );
        return raw == null ? Mono.empty() : parser.parse( raw );

    }

    @Override
    protected Mono<String> getStringArgument( final String name ) {

        return parse( name, RawParser.STRING );

    }

    @Override
    protected Mono<Boolean> getBooleanArgument( final String name ) {

        return parse( name, RawParser.BOOLEAN );

    }

    @Override
    protected Mono<Long> getIntegerArgument( final String name )
            throws InvalidArgumentException {

        return parse( name, RawParser.INTEGER );

    }

    @Override
    protected Mono<Double> getFloatArgument( final String name )
            throws InvalidArgumentException {

        return parse( name, RawParser.FLOAT );

    }

    @Override
    protected Mono<Snowflake> getSnowflakeArgument( final String name,
            final SnowflakeParser.Type type ) throws InvalidArgumentException {

        return parse( name, RawParser.SNOWFLAKE );

    }

    @Override
    protected Mono<User> getUserArgument( final String name ) {

        return parse( name, RawParser.user( this ) );

    }

    @Override
    protected Mono<Role> getRoleArgument( final String name ) {

        return parse( name, RawParser.role( this ) );

    }

    @Override
    protected <C extends @NonNull Channel> Mono<C> getChannelArgument( final String name,
            final Class<C> type ) {

        return parse( name, RawParser.channel( this, type ) );

    }

    @Override
    protected Mono<Attachment> getAttachmentArgument( final String name ) {

        final var attachment = getAttachmentArgs().get( name );
        return attachment == null ? Mono.empty() : Mono.just( attachment );

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
    public MessageCreateEvent getEvent() {

        return event;

    }

}
