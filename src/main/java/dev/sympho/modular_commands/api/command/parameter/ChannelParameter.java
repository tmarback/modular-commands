package dev.sympho.modular_commands.api.command.parameter;

import java.util.regex.Pattern;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.nullness.util.NullnessUtil;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.context.CommandContext;
import dev.sympho.modular_commands.api.exception.InvalidArgumentException;
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
 * @version 1.0
 * @since 1.0
 */
public record ChannelParameter(
        String name, String description,
        boolean required, @Nullable Channel defaultValue
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
     */
    @SideEffectFree
    public ChannelParameter( 
            final String name, final String description, 
            final boolean required, final @Nullable Channel defaultValue
    ) {

        this.name = ParameterUtils.validateName( name );
        this.description = ParameterUtils.validateDescription( description );
        this.required = required;
        this.defaultValue = defaultValue;

    }

    @Override
    public Mono<Channel> fromUrl( final GatewayDiscordClient client, final String url ) 
            throws InvalidArgumentException {
        
        final var matcher = LINK_PATTERN.matcher( url );
        if ( !matcher.matches() ) {
            throw new InvalidArgumentException( this, "Invalid channel URL: %s".formatted( url ) );
        }

        final String channelString = NullnessUtil.castNonNull( matcher.group( 1 ) );

        final Snowflake channelId = Snowflake.of( channelString );

        return client.getChannelById( channelId );

    }

    @Override
    public Mono<Channel> getEntity( final CommandContext context, final Snowflake id ) {

        return context.getGuild().flatMap( g -> g.getChannelById( id ) );

    }

    @Override
    public String parseMention( final String mention ) throws InvalidArgumentException {

        return OptionalUtils.castPresent( MentionableParameter.parseMention( mention, "#" ) )
                .orElseThrow( () -> new InvalidArgumentException( this, 
                        "Not a valid channel mention: <%s>".formatted( mention ) )
                );

    }
    
}
