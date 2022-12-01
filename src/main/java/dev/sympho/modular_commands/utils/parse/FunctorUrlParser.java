package dev.sympho.modular_commands.utils.parse;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.context.CommandContext;
import dev.sympho.modular_commands.api.command.parameter.parse.InvalidArgumentException;
import reactor.core.publisher.Mono;

/**
 * A parser for URL-based arguments that are independent from the invocation context.
 *
 * @param <T> The parsed type.
 * @version 1.0
 * @since 1.0
 */
public interface FunctorUrlParser<T extends @NonNull Object> extends UrlParser<T> {

    /**
     * Creates a parser that delegates to the given parsers.
     *
     * @param <T> The parsed argument type.
     * @param typeName The aggregate type name, for {@link #typeName()}.
     * @param parsers The parsers to delegate to.
     * @return The parser.
     */
    static <T extends @NonNull Object> FunctorUrlParser<T> of( final String typeName,
            final List<? extends FunctorUrlParser<T>> parsers ) {

        return new FunctorChoice<>( typeName, List.copyOf( parsers ) );

    }

    /**
     * Creates a parser that delegates to the given parsers.
     *
     * @param <T> The parsed argument type.
     * @param typeName The aggregate type name, for {@link #typeName()}.
     * @param parsers The parsers to delegate to.
     * @return The parser.
     */
    @SafeVarargs
    @SuppressWarnings( "varargs" )
    static <T extends @NonNull Object> FunctorUrlParser<T> of( final String typeName, 
            final FunctorUrlParser<T>... parsers ) {

        return of( typeName, Arrays.asList( parsers ) );

    }

    /**
     * Parses the given URL.
     *
     * @param url The URL to parse.
     * @return The parsed value. May result in a {@link InvalidArgumentException} if the
     *         URL is invalid.
     * @throws InvalidArgumentException if the URL is invalid.
     */
    @SideEffectFree
    Mono<T> parse( URL url ) throws InvalidArgumentException;

    @Override
    default Mono<T> parse( CommandContext context, URL url ) throws InvalidArgumentException {

        return parse( url );

    }

    /**
     * A parser that supports multiple URL types by delegating to one of a list of parsers.
     * 
     * <p>Note that a parser is chosen as the first in the list whose {@link #supported(URL)}
     * method returns {@code true} for the given URL. This implies that the order of the parsers
     * is relevant if and only if there are URLs that may be parsed by more than one parser in
     * the list.
     *
     * @param <T> The parsed argument type.
     * @param <P> The delegate parser type.
     * @since 1.0
     */
    class FunctorChoice<T extends @NonNull Object, P extends FunctorUrlParser<T>> 
            extends Choice<T, P> implements FunctorUrlParser<T> {

        /**
         * Creates a new instance.
         *
         * @param typeName The aggregate type name, for {@link #typeName()}.
         * @param parsers The parsers to delegate to.
         */
        public FunctorChoice( final String typeName, final List<P> parsers ) {

            super( typeName, parsers );

        }

        @Override
        public Mono<T> parse( final URL url ) throws InvalidArgumentException {

            return getParser( url ).parse( url );

        }

    }
    
}
