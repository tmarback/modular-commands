package dev.sympho.modular_commands.utils.parse;

import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.context.CommandContext;
import dev.sympho.modular_commands.api.command.parameter.parse.InvalidArgumentException;
import reactor.core.publisher.Mono;

/**
 * Convenience specializations of {@link UrlParser} with some predefined functionality.
 * 
 * <p>Multiple of these may be combined by implementing them and overriding any conflicting
 * methods to return a result that is a function of all the parent implementations.
 *
 * @version 1.0
 * @since 1.0
 */
public final class UrlParsers {

    /** Do not instantiate. */
    private UrlParsers() {}

    /**
     * A parser that only accepts URLs with HTTP(S) protocol.
     *
     * @param <T> The parsed argument type.
     * @since 1.0
     */
    public interface Http<T extends @NonNull Object> extends UrlParser<T> {

        /** The accepted protocols. */
        List<String> PROTOCOLS = List.of(
                "http",
                Https.PROTOCOL
        );

        /**
         * @apiNote Implementations probably want to add additional validation to this:
         *     <pre>
         *     {@code
         *     @Override
         *     public boolean supported(final URL url) {
         *       return super.supported() 
         *         && somePath.equals( url.getPath() )
         *         && ...
         *     }
         *     }
         *     </pre>
         */
        @Override
        default boolean supported( final URL url ) {

            return PROTOCOLS.contains( url.getProtocol() );

        }

    }

    /**
     * A parser that only accepts URLs with HTTPS protocol.
     *
     * @param <T> The parsed argument type.
     * @since 1.0
     */
    public interface Https<T extends @NonNull Object> extends UrlParser<T> {

        /** The accepted protocol. */
        String PROTOCOL = "https";

        /**
         * @apiNote Implementations probably want to add additional validation to this:
         *     <pre>
         *     {@code
         *     @Override
         *     public boolean supported(final URL url) {
         *       return super.supported() 
         *         && somePath.equals( url.getPath() )
         *         && ...
         *     }
         *     }
         *     </pre>
         */
        @Override
        default boolean supported( final URL url ) {

            return PROTOCOL.equals( url.getProtocol() );

        }

    }

    /**
     * A parser that accepts URLs from a single host.
     *
     * @param <T> The parsed argument type.
     * @since 1.0
     */
    public interface Host<T extends @NonNull Object> extends UrlParser<T> {

        /**
         * Retrieves the supported host.
         *
         * @return The host.
         */
        @Pure
        String host();

        /**
         * @apiNote Implementations probably want to add additional validation to this:
         *     <pre>
         *     {@code
         *     @Override
         *     public boolean supported(final URL url) {
         *       return super.supported() 
         *         && somePath.equals( url.getPath() )
         *         && ...
         *     }
         *     }
         *     </pre>
         */
        @Override
        default boolean supported( final URL url ) {

            return host().equals( url.getHost() );

        }

    }

    /**
     * A parser that accepts URLs from a set of hosts that are aliases of each other,
     * which means that parsing is independent from which of them is used.
     *
     * @param <T> The parsed argument type.
     * @since 1.0
     */
    public interface HostAlias<T extends @NonNull Object> extends UrlParser<T> {

        /**
         * Retrieves the supported hosts.
         *
         * @return The hosts.
         */
        @Pure
        Set<String> hosts();

        /**
         * @apiNote Implementations probably want to add additional validation to this:
         *     <pre>
         *     {@code
         *     @Override
         *     public boolean supported(final URL url) {
         *       return super.supported() 
         *         && somePath.equals( url.getPath() )
         *         && ...
         *     }
         *     }
         *     </pre>
         */
        @Override
        default boolean supported( final URL url ) {

            return hosts().contains( url.getHost() );

        }

    }

    /**
     * A parser that accepts URLs from a set of hosts that are <i>not</i> aliases of each other,
     * which means that a host-dependent pre-parsing step is applied.
     *
     * @param <T> The parsed argument type.
     * @param <I> The intermediate type generated by the pre-parsing step.
     * @since 1.0
     */
    public interface HostBased<T extends @NonNull Object, I extends @NonNull Object> 
            extends UrlParser<T> {

        /**
         * Retrieves the pre-parser for the given host.
         *
         * @param host The host.
         * @return The pre-parser for the given host, or {@code null} if the host is
         *         not supported.
         */
        @Pure
        @Nullable Function<URL, I> preParser( String host );

        /**
         * @apiNote Implementations probably want to add additional validation to this:
         *     <pre>
         *     {@code
         *     @Override
         *     public boolean supported(final URL url) {
         *       return super.supported() 
         *         && somePath.equals( url.getPath() )
         *         && ...
         *     }
         *     }
         *     </pre>
         */
        @Override
        default boolean supported( final URL url ) {

            return preParser( url.getHost() ) != null;

        }

        /**
         * Parses the value generated by the pre-parsing step.
         *
         * @param context The invocation context.
         * @param value The pre-parsed value.
         * @return The parsed value.
         */
        @SideEffectFree
        Mono<T> parseValue( CommandContext context, I value );

        @Override
        default Mono<T> parse( final CommandContext context, final URL url ) 
                throws InvalidArgumentException {

            final var preParser = preParser( url.getHost() );
            if ( preParser == null ) {
                throw new InvalidArgumentException( "Unsupported host" );
            }
            
            final var value = preParser.apply( url );
            return parseValue( context, value );

        }

    }
    
}
