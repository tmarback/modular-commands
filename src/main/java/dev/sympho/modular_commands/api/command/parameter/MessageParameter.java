package dev.sympho.modular_commands.api.command.parameter;

import java.util.regex.Pattern;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.context.CommandContext;
import dev.sympho.modular_commands.api.exception.InvalidArgumentException;
import dev.sympho.modular_commands.utils.ParameterUtils;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;

/**
 * Specification for a parameter that receives a Discord message.
 *
 * @param name The name of the parameter.
 * @param description The description of the parameter.
 * @param required Whether the parameter must be specified to invoke the command.
 * @param defaultValue The default value for the parameter.
 * @version 1.0
 * @since 1.0
 */
public record MessageParameter(
        String name, String description,
        boolean required, @Nullable Message defaultValue
) implements EntityParameter<Message> {

    private static final Pattern LINK_PATTERN = Pattern.compile(
        "https://discord.com/channels/\\d+/(\\d+)/(\\d+)"
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
    public MessageParameter( 
            final String name, final String description, 
            final boolean required, final @Nullable Message defaultValue
    ) {

        this.name = ParameterUtils.validateName( name );
        this.description = ParameterUtils.validateDescription( description );
        this.required = required;
        this.defaultValue = defaultValue;

    }

    @Override
    public Mono<Message> fromUrl( final GatewayDiscordClient client, final String url ) 
            throws InvalidArgumentException {
        
        final var matcher = LINK_PATTERN.matcher( url );
        if ( !matcher.matches() ) {
            throw new InvalidArgumentException( this, "Invalid channel URL: %s".formatted( url ) );
        }

        final Snowflake channelId = Snowflake.of( matcher.group( 1 ) );
        final Snowflake messageId = Snowflake.of( matcher.group( 2 ) );

        return client.getMessageById( channelId, messageId );

    }

    @Override
    public Mono<Message> getEntity( final CommandContext context, final Snowflake id ) {

        return context.getChannel().flatMap( ch -> ch.getMessageById( id ) );

    }
    
}
