package dev.sympho.modular_commands.api.command;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Spliterator;
import java.util.stream.Stream;

import org.apache.commons.collections4.ListUtils;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

/**
 * An invocation of a command. That is, a sequence of keys (names) that (possibly) map
 * to a command in the system.
 *
 * @param chain The invocation chain, the sequence of keys that forms the invocation.
 * @version 1.0
 * @since 1.0
 */
public record Invocation( List<String> chain ) implements Iterable<String> {

    /**
     * Constructs an invocation determined by the given chain.
     *
     * @param chain The invocation chain.
     */
    @Pure
    public Invocation( final List<String> chain ) {

        chain.forEach( c -> Objects.requireNonNull( c, 
                "Invocation chain cannot contain null." ) );
        this.chain = List.copyOf( chain );

    }

    /**
     * Determines the invocation formed by adding the given command name to
     * this invocation.
     *
     * @param command The child command to invoke.
     * @return The invocation of the child command.
     */
    @Pure
    public Invocation child( final String command ) {

        final var childChain = List.of( 
                Objects.requireNonNull( command, "Command cannot be null." ) 
        );
        return new Invocation( ListUtils.union( chain, childChain ) );

    }

    /**
     * Determines the parent invocation of this chain.
     *
     * @return The invocation of the parent command.
     */
    @Pure
    public Invocation parent() {

        return new Invocation( chain.subList( 0, chain.size() - 1 ) );

    }

    /**
     * Constructs an invocation from the given sequence of command names.
     *
     * @param commands The command names.
     * @return The constructed invocation.
     */
    @Pure
    public static Invocation of( final List<String> commands ) {

        return new Invocation( commands );

    }

    /**
     * Constructs an invocation from the given sequence of command names.
     *
     * @param commands The command names.
     * @return The constructed invocation.
     */
    @Pure
    public static Invocation of( final String... commands ) {

        return of( Arrays.asList( commands ) );

    }

    @Override
    public Iterator<String> iterator() {

        return chain.iterator();

    }

    @Override
    public Spliterator<String> spliterator() {
        
        return chain.spliterator();

    }

    /**
     * Obtains a stream over the invocation elements.
     *
     * @return The stream.
     */
    @SideEffectFree
    public Stream<String> stream() {
        
        return chain.stream();

    }
    
}
