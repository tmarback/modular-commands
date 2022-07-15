package dev.sympho.modular_commands.api.permission;

import org.checkerframework.dataflow.qual.SideEffectFree;

import dev.sympho.modular_commands.api.command.result.CommandResult;
import reactor.core.publisher.Mono;

/**
 * Validator that determines whether a caller has certain access rights in the
 * context of an invocation.
 *
 * @version 1.0
 * @since 1.0
 * @apiNote This type should usually not be manually created during command handling;
 *          Rather, it should be obtain from the execution context in order to respect
 *          current configuration.
 */
@FunctionalInterface
public interface AccessValidator {

    /**
     * Determines whether the invoking user in the current execution context (guild and
     * channel) has access equivalent to the given group, otherwise generating an 
     * appropriate result.
     * 
     * <p>Note that while the most straightforward implementation of this interface is
     * to simply check if the caller 
     * {@link Group#belongs(Mono, Mono, discord4j.core.object.entity.User) belongs}
     * to the given group, implementations are allowed to add other conditions under
     * which a user has equivalent permissions despite not belonging to the group
     * (or conversely does <i>not</i> have permissions despite <i>belonging</i> to
     * the group).
     *
     * @param group The group required for access.
     * @return A Mono that is empty if the caller has access equivalent to the given
     *         group under the current execution context, or otherwise issues a 
     *         failure result.
     */
    @SideEffectFree
    Mono<CommandResult> validate( Group group );
    
}
