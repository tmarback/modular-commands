package dev.sympho.modular_commands.api.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.ListUtils;
import org.checkerframework.dataflow.qual.Pure;

/**
 * An invocation of a command. That is, a sequence of keys (names) that (possibly) map
 * to a command in the system.
 *
 * @param chain The invocation chain, the sequence of keys that forms the invocation.
 * @version 1.0
 * @since 1.0
 */
public record Invocation( List<String> chain ) {

    /**
     * Constructs an invocation determined by the given chain.
     *
     * @param chain The invocation chain.
     */
    @Pure
    public Invocation( final List<String> chain ) {

        this.chain = Collections.unmodifiableList( new ArrayList<>( chain ) );

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

        return new Invocation( ListUtils.union( chain, List.of( command ) ) );

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
    public static Invocation of( final String... commands ) {

        return new Invocation( Arrays.asList( commands ) );

    }
    
}
