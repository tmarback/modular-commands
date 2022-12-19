package dev.sympho.modular_commands.execute;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.LoggerFactory;

import dev.sympho.modular_commands.api.command.Invocation;
import dev.sympho.modular_commands.utils.StringSplitter.Async.Iterator;

/**
 * Provides aliases that apply to message-based commands.
 *
 * @version 1.0
 * @since 1.0
 */
@FunctionalInterface
public interface AliasProvider extends UnaryOperator<Iterator> {

    /**
     * Applies aliases to the given iterator over received raw tokens.
     *
     * @param iter The arg iterator to apply aliases to.
     * @return The iterator over the equivalent invocation with any aliases applied.
     * @implSpec Implementations should <b>not</b> consume all elements in the given iterator
     *           before checking for aliases, both due to performance concerns and due to
     *           interfering with argument parsing. More specifically, the value of
     *           {@link Iterator#remainder()} after the aliased portion should be exactly
     *           the same before or after applying the alias.
     */
    @Override
    Iterator apply( Iterator iter );

    /**
     * Creates an alias provider with the given aliases.
     *
     * @param aliases The aliases to use in the created provider.
     * @return The constructed provider.
     */
    static AliasProvider of( final Collection<? extends Entry<Invocation, Invocation>> aliases ) {

        final var map = aliases.stream().collect( 
                Collectors.toUnmodifiableMap( Entry::getKey, Entry::getValue ) 
        );

        return iter -> {

            var current = Invocation.of();
            @Nullable Invocation result = null;
    
            while ( iter.hasNext() ) {
    
                final var next = current.child( iter.peek() );
                final var alias = map.get( next );
                if ( alias == null ) {
                    break;
                }
    
                current = next;
                result = alias;
                iter.next();
    
            }
    
            if ( result == null ) {
                return iter;
            }
    
            LoggerFactory.getLogger( AliasProvider.class ).debug( 
                    "Applying alias from {} to {}", current, result 
            );
    
            final var splitter = iter.splitter();
            final var alias = String.join( splitter.delimiter(), result.chain() );
            final var aliased = String.join( splitter.delimiter(), alias, iter.remainder() );
            return splitter.iterate( aliased );

        };

    }

    /**
     * Creates an alias provider with the given aliases.
     *
     * @param aliases The aliases to use in the created provider.
     * @return The constructed provider.
     */
    static AliasProvider of( final Map<Invocation, Invocation> aliases ) {

        return of( aliases.entrySet() );

    }

    /**
     * Creates an alias provider with the given aliases.
     *
     * @param aliases The aliases to use in the created provider.
     * @return The constructed provider.
     */
    @SafeVarargs
    @SuppressWarnings( "varargs" )
    static AliasProvider of( final Entry<Invocation, Invocation>... aliases ) {

        return of( Arrays.asList( aliases ) );

    }

    /**
     * Creates an alias provider with no aliases.
     *
     * @return The constructed provider.
     */
    static AliasProvider none() {

        return iter -> iter;

    }
    
}
