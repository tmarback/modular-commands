package dev.sympho.modular_commands.api.command.parameter;

import java.util.Objects;
import java.util.regex.Pattern;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.nullness.util.NullnessUtil;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.context.CommandContext;
import dev.sympho.modular_commands.api.command.parameter.parse.InvalidArgumentException;
import dev.sympho.modular_commands.utils.OptionalUtils;
import dev.sympho.modular_commands.utils.ParameterUtils;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.Channel;
import reactor.core.publisher.Mono;

/**
 * Specification for a parameter that receives a Discord channel.
 *
 * @param name The name of the parameter.
 * @param description The description of the parameter.
 * @param required Whether the parameter must be specified to invoke the command.
 * @param defaultValue The default value for the parameter.
 * @param type The type of channel.
 * @version 1.0
 * @since 1.0
 */
public record ChannelParameter(
        String name, String description,
        boolean required, @Nullable Channel defaultValue,
        Class<? extends Channel> type
) implements MentionableParameter<Channel> {

    /** Link URL pattern. */
    private static final Pattern LINK_PATTERN = Pattern.compile(
            "https://discord.com/channels/\\d+/(\\d+)"
    );

    /**
     * Creates a new instance.
     *
     * @param name The name of the parameter.
     * @param description The description of the parameter.
     * @param required Whether the parameter must be specified to invoke the command.
     * @param defaultValue The default value for the parameter.
     * @param type The type of channel.
     */
    @SideEffectFree
    public ChannelParameter( 
            final String name, final String description, 
            final boolean required, final @Nullable Channel defaultValue,
            final Class<? extends Channel> type
    ) {

        this.name = ParameterUtils.validateName( name );
        this.description = ParameterUtils.validateDescription( description );
        this.required = required;
        this.defaultValue = defaultValue;
        this.type = Objects.requireNonNull( type );

    }

    /**
     * Validates that a channel is of the expected type.
     *
     * @param channel The channel.
     * @return The channel.
     * @throws InvalidArgumentException if the channel does not match the expected type.
     */
    private Channel validate( final Channel channel ) throws InvalidArgumentException {

        try {
            return type.cast( channel );
        } catch ( final ClassCastException e ) {
            throw new InvalidArgumentException( "Channel must be a %s".formatted( 
                    type.getSimpleName() ) );
        }

    }

    @Override
    public Mono<Channel> fromUrl( final GatewayDiscordClient client, final String url ) 
            throws InvalidArgumentException {
        
        final var matcher = LINK_PATTERN.matcher( url );
        if ( !matcher.matches() ) {
            throw new InvalidArgumentException( "Invalid channel URL: %s".formatted( url ) );
        }

        final String channelString = NullnessUtil.castNonNull( matcher.group( 1 ) );

        final Snowflake channelId = Snowflake.of( channelString );

        return client.getChannelById( channelId ).map( this::validate );

    }

    @Override
    public Mono<Channel> getEntity( final CommandContext context, final Snowflake id ) {

        return context.getGuild().flatMap( g -> g.getChannelById( id ) ).map( this::validate );

    }

    @Override
    public String parseMention( final String mention ) throws InvalidArgumentException {

        return OptionalUtils.castPresent( MentionableParameter.parseMention( mention, "#" ) )
                .orElseThrow( () -> new InvalidArgumentException(
                        "Not a valid channel mention: <%s>".formatted( mention ) )
                );

    }

    /**
     * Retrives the channel type.
     *
     * @return The channel type.
     */
    public Class<? extends Channel> getType() {

        return type;

    }
    
}
