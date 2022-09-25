package dev.sympho.modular_commands.impl.context;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.Streams;

import org.apache.commons.collections4.ListUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.Invocation;
import dev.sympho.modular_commands.api.command.ReplyManager;
import dev.sympho.modular_commands.api.command.context.MessageCommandContext;
import dev.sympho.modular_commands.api.command.parameter.Parameter;
import dev.sympho.modular_commands.api.command.parameter.parse.ArgumentParser;
import dev.sympho.modular_commands.api.command.parameter.parse.AttachmentParser;
import dev.sympho.modular_commands.api.command.parameter.parse.ChoicesParser;
import dev.sympho.modular_commands.api.command.parameter.parse.InputParser;
import dev.sympho.modular_commands.api.command.parameter.parse.InvalidArgumentException;
import dev.sympho.modular_commands.api.command.parameter.parse.NumberParser;
import dev.sympho.modular_commands.api.command.parameter.parse.SnowflakeParser;
import dev.sympho.modular_commands.api.command.parameter.parse.StringParser;
import dev.sympho.modular_commands.api.command.result.CommandFailureArgumentExtra;
import dev.sympho.modular_commands.api.exception.ResultException;
import dev.sympho.modular_commands.api.permission.AccessValidator;
import dev.sympho.modular_commands.utils.parse.ChannelParser;
import dev.sympho.modular_commands.utils.parse.RoleParser;
import dev.sympho.modular_commands.utils.parse.UserParser;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.MessageChannel;
import reactor.core.publisher.Mono;

/**
 * Context object for invocations performed through text messages.
 *
 * @version 1.0
 * @since 1.0
 */
public final class MessageContextImpl extends ContextImpl<String> implements MessageCommandContext {

    /** The user parser. */
    private static final UserParser USER_PARSER = new UserParser();

    /** The channel parser. */
    private static final RoleParser ROLE_PARSER = new RoleParser();

    /** The event that triggered the invocation. */
    private final MessageCreateEvent event;

    /** The arguments received as text. */
    private final Map<String, String> inputArgs;

    /** The arguments received as attachments. */
    private final Map<String, Attachment> attachmentArgs;

    /**
     * Initializes a new context.
     *
     * @param event The event that triggered the invocation.
     * @param invocation The invocation that triggered execution.
     * @param parameters The command parameters.
     * @param args The raw arguments received.
     * @param access The validator to use for access checks.
     * @throws ResultException if there is a mismatch between parameters and arguments.
     */
    public MessageContextImpl( final MessageCreateEvent event, final Invocation invocation, 
            final List<Parameter<?>> parameters, final List<String> args, 
            final AccessValidator access ) throws ResultException {

        super( invocation, parameters, access );

        this.event = event;

        final var inputParams = parameters.stream()
                .filter( p -> p.parser() instanceof InputParser<?, ?> )
                .toList();
        final var attachmentParams = parameters.stream()
                .filter( p -> p.parser() instanceof AttachmentParser<?> )
                .toList();

        final var adjustedArgs = adjustArgs( inputParams, args );
        this.inputArgs = assign( inputParams, adjustedArgs, Function.identity() );

        final var attachments = event.getMessage().getAttachments();
        this.attachmentArgs = assign( attachmentParams, attachments, Attachment::getFilename );

    }

    /**
     * Assigns arguments to the corresponding parameters.
     *
     * @param <T> The raw argument type.
     * @param parameters The parameters.
     * @param arguments The arguments to assign.
     * @param toString The function to use to convert an argument into an identifier
     *                 for error messages.
     * @return The arguments, keyed by the corresponding parameter name.
     * @throws ResultException if there is a mismatch.
     */
    @SideEffectFree
    private static <T> Map<String, T> assign( final List<? extends Parameter<?>> parameters, 
            final List<T> arguments, final Function<T, String> toString ) throws ResultException {

        final var pairs = Streams.zip( 
                parameters.stream().map( Parameter::name ),
                arguments.stream(), 
                Map::entry 
        );
        
        final var merged = pairs.collect( Collectors.toMap( Entry::getKey, Entry::getValue ) );

        if ( merged.size() < arguments.size() ) {
            final var extra = arguments.stream()
                    .skip( merged.size() )
                    .map( toString )
                    .toList();
            throw new ResultException( new CommandFailureArgumentExtra( extra ) );
        }

        return merged;

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
    @SideEffectFree
    private static List<String> adjustArgs( final List<Parameter<?>> parameters, 
            final List<String> args ) {

        if ( args.size() <= parameters.size() ) {
            return args;
        }

        if ( parameters.isEmpty() ) {
            return args;
        }

        final var last = parameters.size() - 1;
        if ( parameters.get( last ).parser() instanceof StringParser<?> ) {
            final List<String> head = args.subList( 0, last );
            final List<String> tail = args.subList( last, args.size() );
            final String expandedLast = String.join( " ", tail );
            return ListUtils.union( head, List.of( expandedLast ) );
        }

        return args;

    }

    @Override
    protected Mono<ReplyManager> makeReplyManager() {

        final var message = getMessageEvent().getMessage();
        final var caller = getCaller();
        return Mono.zip( getChannel(), caller.getPrivateChannel() )
                .map( t -> {
                    
                    final var publicChannel = t.getT1();
                    final var privateChannel = t.getT2();
                    return new MessageReplyManager( message, publicChannel, privateChannel );

                } );
        
    }

    /**
     * Parses a raw input argument.
     *
     * @param <P> The raw value type.
     * @param name The parameter name.
     * @param parser The parser function.
     * @param errorMessage The message to use in case of errors.
     * @return The argument, or {@code null} if missing.
     * @throws InvalidArgumentException if the argument is invalid.
     */
    @SuppressWarnings( "IllegalCatch" )
    private <P extends @NonNull Object> @Nullable P parseSync( final String name,
            final Function<String, P> parser, final String errorMessage ) 
            throws InvalidArgumentException {

        try {
            final var raw = inputArgs.get( name );
            return raw == null ? null : parser.apply( raw );
        } catch ( final RuntimeException ex ) {
            throw new InvalidArgumentException( errorMessage, ex );
        }

    }

    /**
     * Parses a raw input argument.
     *
     * @param <P> The raw value type.
     * @param name The parameter name.
     * @param parser The parser function.
     * @return The argument, or empty if missing.
     */
    private <P extends @NonNull Object> Mono<P> parseAsync( final String name,
            final Function<String, Mono<P>> parser ) {

        final var raw = inputArgs.get( name );
        return raw == null ? Mono.empty() : parser.apply( raw );

    }

    @Override
    protected @Nullable String getStringArgument( final String name ) {
        return inputArgs.get( name );
    }

    @Override
    protected @Nullable Long getIntegerArgument( final String name )
            throws InvalidArgumentException {

        return parseSync( name, Long::valueOf, "Not a valid integer" );

    }

    @Override
    protected @Nullable Double getFloatArgument( final String name )
            throws InvalidArgumentException {

        return parseSync( name, Double::valueOf, "Not a valid number" );

    }

    @Override
    protected @Nullable Snowflake getSnowflakeArgument( final String name, 
            final SnowflakeParser.Type type ) throws InvalidArgumentException {
            
        return parseSync( name, Snowflake::of, "Not a valid snowflake" );

    }

    @Override
    protected Mono<User> getUserArgument( final String name ) {
        return parseAsync( name, raw -> USER_PARSER.parse( this, raw ) );
    }

    @Override
    protected Mono<Role> getRoleArgument( final String name ) {
        return parseAsync( name, raw -> ROLE_PARSER.parse( this, raw ) );
    }

    @Override
    protected <C extends @NonNull Channel> Mono<C> getChannelArgument( final String name, 
            final Class<C> type ) {
        // Can't use a static parser due to the generics
        return parseAsync( name, raw -> new ChannelParser<>( type ).parse( this, raw ) );
    }

    @Override
    protected @Nullable Attachment getAttachmentArgument( final String name ) {
        return attachmentArgs.get( name );
    }

    @Override
    protected <R extends @NonNull Object> R validateRaw( final ArgumentParser<R, ?> parser,
            final R raw ) throws InvalidArgumentException {

        if ( parser instanceof ChoicesParser<R, ?> p ) {
            p.verifyChoice( raw );
        }

        if ( parser instanceof NumberParser<?, ?> p ) {
            p.verifyInRangeCast( raw );
        }

        if ( parser instanceof StringParser<?> p ) {
            p.verifyLength( ( String ) raw );
        }

        return raw;

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
