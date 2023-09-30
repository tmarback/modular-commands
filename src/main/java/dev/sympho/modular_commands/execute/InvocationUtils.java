package dev.sympho.modular_commands.execute;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.bot_utils.access.Group;
import dev.sympho.modular_commands.api.command.Command;
import dev.sympho.modular_commands.api.command.Invocation;
import dev.sympho.modular_commands.api.command.handler.Handlers;
import dev.sympho.modular_commands.api.command.parameter.Parameter;
import dev.sympho.modular_commands.api.exception.InvalidChainException;
import dev.sympho.modular_commands.api.registry.Registry;
import dev.sympho.modular_commands.utils.SmartIterator;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * Utility functions for handling invocations.
 *
 * @version 1.0
 * @since 1.0
 */
public final class InvocationUtils {

    /** Do not instantiate. */
    private InvocationUtils() {}

    /**
     * Extracts an invocation from a sequence of args by performing lookups on the given
     * registry, while building the corresponding execution chain.
     * 
     * <p>After this method returns, the given {@code args} iterator will be positioned
     * such that the next element is the first argument that did not match a subcommand
     * (and thus the first proper argument).
     * 
     * <p>Matching is performed case-insensitive by converting received args to lowercase
     * before attempting to match (command names are always lowercase).
     *
     * @param <H> The handler type.
     * @param registry The registry to use for command lookups.
     * @param args The invocation args.
     * @param commandType The command type.
     * @return The detected invocation and the corresponding execution chain.
     */
    @SideEffectFree
    public static <H extends Handlers> Tuple2<Invocation, List<Command<? extends H>>> 
            parseInvocation( final Registry registry, final SmartIterator<String> args, 
            final Class<H> commandType ) {

        final List<Command<? extends H>> chain = new LinkedList<>();
        Invocation current = Invocation.of();
        while ( args.hasNext() ) {

            final var arg = args.peek().toLowerCase( Locale.ROOT ); // Ignore case
            final var next = current.child( arg );
            final var command = registry.findCommand( next, commandType );
            if ( command == null ) {
                break;
            } else {
                chain.add( command );
                current = current.child( command.name() );
                args.next();
            }

        }
        return Tuples.of( current, List.copyOf( chain ) );

    }

    /**
     * Extracts the command being invoked from an execution chain.
     *
     * @param <C> The command type.
     * @param chain The execution chain.
     * @return The command that was invoked (the last one in the chain).
     */
    @Pure
    public static <C extends Command<?>> C getInvokedCommand( final List<? extends C> chain ) {

        return chain.get( chain.size() - 1 );

    }

    /**
     * Determines the command in the execution chain that should provide the
     * invocation settings.
     *
     * @param <C> The command type.
     * @param chain The command chain.
     * @return The command to take settings from.
     * @see Command#inheritSettings()
     */
    @Pure
    public static <C extends @NonNull Command<?>> C getSettingsSource( final List<C> chain ) {

        return Lists.reverse( chain ).stream()
                .filter( Predicate.not( Command::inheritSettings ) )
                .findFirst()
                .orElse( chain.get( 0 ) );

    }

    /**
     * Determines the total set of groups required for an execution chain.
     *
     * @param chain The execution chain.
     * @return The required groups.
     */
    @SideEffectFree
    public static List<Group> accumulateGroups( 
            final List<? extends Command<?>> chain ) {

        // https://github.com/typetools/checker-framework/issues/4048
        @SuppressWarnings( "type.argument" )
        final var needParent = Lists.reverse( chain ).stream()
                .takeWhile( Command::requireParentGroups )
                .count();

        // Need to include the parent of the last command to require parent, unless all
        // of them do (clamp at chain size)
        final var take = Math.min( needParent + 1, chain.size() );

        return chain.stream()
                .skip( chain.size() - take )
                .map( Command::requiredGroup )
                .toList();

    }

    /**
     * Determines if a parameter is always satisfied.
     *
     * @param parameter The parameter.
     * @return If the parameter is always satisfied.
     */
    private static boolean satisfied( final Parameter<?> parameter ) {

        return parameter.required() || parameter.defaultValue() != null;

    }

    /**
     * Determines the sequence that command handlers should be invoked in for
     * the given chain.
     *
     * @param <C> The command type.
     * @param chain The invocation chain.
     * @return The commands, in the order that their handlers must be executed.
     *         Some of the commands in the chain may be missing if they do not
     *         need to be executed.
     * @throws InvalidChainException if the command chain is incompatible.
     * @see Command#invokeParent()
     */
    @SideEffectFree
    public static <C extends @NonNull Command<?>> List<C> handlingOrder( final List<C> chain ) {

        final List<C> commands = new LinkedList<>();
        final var it = chain.listIterator( chain.size() );

        C source = it.previous();
        commands.add( source );

        final C target = source;
        final Set<String> satisfiedParameters = source.parameters().stream()
                .filter( InvocationUtils::satisfied )
                .map( Parameter::name )
                .collect( Collectors.toSet() );
        
        @SuppressWarnings( "rawtypes" ) // Generics is dumb with Class<>
        final Map<String, Class<? extends Parameter>> parameterTypes = source.parameters().stream()
                .collect( Collectors.toMap( Parameter::name, Parameter::getClass ) );

        while ( it.hasPrevious() && source.invokeParent() ) {

            source = it.previous();
            // Validate command compatibility
            for ( final var p : source.parameters() ) {

                final var receivedType = parameterTypes.get( p.name() );
                // Validate type
                if ( receivedType != null && receivedType != p.getClass() ) {
                    throw new InvalidChainException( source, target, String.format( 
                            "Parameter %s is of type %s in parent but type %s in child", 
                            p.name(), p.getClass().getSimpleName(), receivedType.getSimpleName()
                    ) );
                }
                // Validate presence
                if ( p.required() && !satisfiedParameters.contains( p.name() ) ) {
                    throw new InvalidChainException( source, target, String.format( 
                            "Parameter %s is required in parent but may not be present in child", 
                            p.name()
                    ) );
                }

            }

            commands.add( 0, source );

        }

        return List.copyOf( commands );

    }

    /**
     * Determines if a command has handlers compatible with the given type.
     *
     * @param <H> The handler type.
     * @param command The command to check.
     * @param type The handler type.
     * @return The command, or {@code null} if the command's handlers are not
     *         compatible.
     */
    @Pure
    @SuppressWarnings( "unchecked" )
    public static <H extends Handlers> @Nullable Command<? extends H> checkType(
            final Command<?> command, final Class<H> type
    ) {

        return type.isInstance( command.handlers() ) 
                ? ( Command<? extends H> ) command 
                : null;

    }
    
}
