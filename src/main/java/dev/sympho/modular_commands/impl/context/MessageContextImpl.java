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
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.bot_utils.access.AccessManager;
import dev.sympho.bot_utils.event.reply.ReplyManager;
import dev.sympho.modular_commands.api.command.Command;
import dev.sympho.modular_commands.api.command.Invocation;
import dev.sympho.modular_commands.api.command.context.MessageCommandContext;
import dev.sympho.modular_commands.api.command.parameter.Parameter;
import dev.sympho.modular_commands.api.command.parameter.parse.AttachmentParser;
import dev.sympho.modular_commands.api.command.parameter.parse.InputParser;
import dev.sympho.modular_commands.api.command.parameter.parse.InvalidArgumentException;
import dev.sympho.modular_commands.api.command.parameter.parse.SnowflakeParser;
import dev.sympho.modular_commands.api.command.parameter.parse.StringParser;
import dev.sympho.modular_commands.api.command.result.CommandFailureArgumentExtra;
import dev.sympho.modular_commands.api.exception.ResultException;
import dev.sympho.modular_commands.execute.LazyContext;
import dev.sympho.modular_commands.execute.Metrics;
import dev.sympho.modular_commands.utils.StringSplitter.Async.Iterator;
import dev.sympho.modular_commands.utils.parse.RawParser;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Channel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Context object for invocations performed through text messages.
 *
 * @version 1.0
 * @since 1.0
 */
public final class MessageContextImpl extends ContextImpl<String, MessageCreateEvent> 
        implements MessageCommandContext {

    /** The received inline arguments. */
    private final Iterator arguments;

    /** The raw argument string. */
    private final String argString;

    /** The arguments received as text, in the order received. */
    private @MonotonicNonNull List<String> inputArgsList;

    /** The arguments received as text. */
    private @MonotonicNonNull Map<String, String> inputArgs;

    /** The arguments received as attachments. */
    private @MonotonicNonNull Map<String, Attachment> attachmentArgs;

    /**
     * Initializes a new context.
     *
     * @param event The event that triggered the invocation.
     * @param invocation The invocation that triggered execution.
     * @param command The invoked command.
     * @param args The raw arguments received.
     * @param accessManager The access manager to use.
     * @param replyManager The reply manager to use.
     * @throws ResultException if there is a mismatch between parameters and
     *                         arguments.
     */
    public MessageContextImpl( 
            final MessageCreateEvent event, 
            final Invocation invocation, final Command<?> command, 
            final Iterator args, 
            final AccessManager accessManager, final ReplyManager replyManager 
    ) throws ResultException {

        super( event, invocation, command, accessManager, replyManager );

        this.arguments = args;
        this.argString = args.remainder(); // Save the arg string

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

        if ( arguments.size() > parameters.size() ) {

            final var extra = arguments.stream()
                    .skip( parameters.size() )
                    .map( toString )
                    .toList();
            throw new ResultException( new CommandFailureArgumentExtra( extra ) );

        }

        return Streams.zip( 
                    parameters.stream().map( Parameter::name ),
                    arguments.stream(), 
                    Map::entry 
                )
                .collect( Collectors.toMap( 
                    Entry::getKey, 
                    Entry::getValue 
                ) );

    }

    /**
     * Adjusts the args received so that trailing arguments are merged into the
     * final argument, if the final parameter is a string and allows merging.
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

            final var inputParams = command.parameters().stream()
                    .filter( p -> p.parser() instanceof InputParser<?, ?> ).toList();
            final var attachmentParams = command.parameters().stream()
                    .filter( p -> p.parser() instanceof AttachmentParser<?> ).toList();

            final var attachments = event().getMessage().getAttachments();
            this.attachmentArgs = assign( attachmentParams, attachments, Attachment::getFilename );

            return adjustArgs( inputParams, arguments )
                    .collectList()
                    .map( Collections::unmodifiableList )
                    .doOnNext( args -> {
                        this.inputArgsList = args;
                    } )
                    .map( args -> assign( inputParams, args, Function.identity() ) )
                    .map( Collections::unmodifiableMap )
                    .doOnNext( args -> { 
                        this.inputArgs = args; 
                    } )
                    .then();

        } );

    }

    @Override
    public Metrics.Tag.Type tagType() {
        return Metrics.Tag.Type.MESSAGE;
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

        return parse( name, switch ( type ) {
            case ANY -> RawParser.SNOWFLAKE;
            case CHANNEL -> RawParser.channelId( this );
            case MESSAGE -> RawParser.messageId( this );
            case ROLE -> RawParser.roleId( this );
            case USER -> RawParser.userId( this );
        } );

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
    public String argString() {

        return argString;

    }

    @Override
    public List<String> rawArgs() {

        if ( inputArgsList == null ) {
            throw LazyContext.notLoadedError();
        }
        return inputArgsList;

    }

    @Override
    public Map<String, String> rawArgMap() {

        if ( inputArgs == null ) {
            throw LazyContext.notLoadedError();
        }
        return inputArgs;

    }

}
