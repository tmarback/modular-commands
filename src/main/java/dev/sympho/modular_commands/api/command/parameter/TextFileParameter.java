package dev.sympho.modular_commands.api.command.parameter;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.common.value.qual.IntRange;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.context.CommandContext;
import dev.sympho.modular_commands.api.command.parameter.parse.InvalidArgumentException;
import dev.sympho.modular_commands.utils.ParameterUtils;
import reactor.core.publisher.Mono;

/**
 * Specification for a text file parameter.
 *
 * @param name The name of the parameter.
 * @param description The description of the parameter.
 * @param required Whether the parameter must be specified to invoke the command.
 * @param defaultValue The default value for the parameter.
 * @param maxSize The maximum size of text file accepted (in bytes).
 * @version 1.0
 * @since 1.0
 */
public record TextFileParameter(
        String name, String description, 
        boolean required, @Nullable String defaultValue, 
        @IntRange( from = 0 ) int maxSize
) implements TextAttachmentParameter<String> {

    /**
     * Creates a new instance.
     *
     * @param name The name of the parameter.
     * @param description The description of the parameter.
     * @param required Whether the parameter must be specified to invoke the command.
     * @param defaultValue The default value for the parameter.
     * @param maxSize The maximum size of text file accepted (in bytes).
     */
    @SideEffectFree
    public TextFileParameter( 
            final String name, final String description, 
            final boolean required, final @Nullable String defaultValue, 
            final @IntRange( from = 0 ) int maxSize
    ) {

        this.name = ParameterUtils.validateName( name );
        this.description = ParameterUtils.validateDescription( description );
        this.required = required;
        this.defaultValue = defaultValue;
        this.maxSize = Math.max( 0, maxSize ); // Clamp to [0, infty)

    }

    @Override
    public Mono<String> parse( final CommandContext context, final String content )
            throws InvalidArgumentException {

        return Mono.just( content );

    }
    
}
