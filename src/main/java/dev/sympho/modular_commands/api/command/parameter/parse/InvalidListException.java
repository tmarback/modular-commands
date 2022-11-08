package dev.sympho.modular_commands.api.command.parameter.parse;

import java.util.List;

import dev.sympho.modular_commands.utils.parse.TryParser;

/**
 * Exception that indicates that some items in an argument list were invalid.
 *
 * @version 1.0
 * @since 1.0
 */
public class InvalidListException extends InvalidArgumentException {

    private static final long serialVersionUID = -8131494020117717815L;

    /** The items that had errors. */
    private final List<TryParser.Failure<?, ?>> errors;

    /**
     * Creates a new instance.
     *
     * @param errors The items that had errors.
     */
    public InvalidListException( final List<? extends TryParser.Failure<?, ?>> errors ) {

        super( "Some items were not valid" );

        this.errors = List.copyOf( errors );
        
    }

    /**
     * Retrieves the items that had errors.
     *
     * @return The items that had errors.
     */
    public List<TryParser.Failure<?, ?>> errors() {
        return errors;
    }

    @Override
    public String toString() {

        return errors().stream()
                .map( e -> e.raw() + ": " + e.error().getMessage() )
                .toList()
                .toString();

    }
    
}
