package dev.sympho.modular_commands;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import dev.sympho.modular_commands.api.command.Command;
import dev.sympho.modular_commands.api.command.handler.Handlers;
import dev.sympho.modular_commands.api.command.handler.MessageHandlers;
import dev.sympho.modular_commands.api.command.result.Results;
import dev.sympho.modular_commands.api.permission.Group;
import dev.sympho.modular_commands.api.permission.Groups;
import dev.sympho.modular_commands.execute.InvocationUtils;
import dev.sympho.modular_commands.utils.builder.CommandBuilder;
import discord4j.common.util.Snowflake;

/**
 * Test driver for {@link InvocationUtils}.
 *
 * @version 1.0
 * @since 1.0
 */
public class InvocationUtilsTest {

    /** The first test group. */
    private static final Group group1 = Groups.isUser( Snowflake.of( 123 ) );
    /** The second test group. */
    private static final Group group2 = Groups.hasRole( Snowflake.of( 123 ) );
    /** The third test group. */
    private static final Group group3 = Groups.hasRolesAll( Snowflake.of( 123 ) );

    /**
     * Pre-configures a command builder.
     *
     * @return The builder.
     */
    private static CommandBuilder<MessageHandlers> baseBuilder() {

        return CommandBuilder.message()
                .withName( "test" )
                .withDisplayName( "test" )
                .withDescription( "test" );

    }

    /**
     * Configures a command for group tests.
     *
     * @param group The group to require.
     * @param parent Whether to require parent groups.
     * @return The command.
     */
    private static Command<MessageHandlers> groupTestCommand( 
            final Group group, final boolean parent ) {

        return baseBuilder()
                .requireGroup( group )
                .setRequireParentGroups( parent )
                .withHandlers( Handlers.message( ctx -> Results.okMono() ) )
                .build();

    }

    /**
     * Tests {@link InvocationUtils#accumulateGroups(List)} with a single command that
     * requires parent groups.
     */
    @Test
    public void testGroupAccumulateSingleTrue() {

        final var command = groupTestCommand( group1, true );
        assertThat( InvocationUtils.accumulateGroups( List.of( command ) ) )
                .containsExactly( group1 );
        
    }

    /**
     * Tests {@link InvocationUtils#accumulateGroups(List)} with a single command that
     * does not require parent groups.
     */
    @Test
    public void testGroupAccumulateSingleFalse() {

        final var command = groupTestCommand( group2, false );
        assertThat( InvocationUtils.accumulateGroups( List.of( command ) ) )
                .containsExactly( group2 );
        
    }

    /**
     * Tests {@link InvocationUtils#accumulateGroups(List)} with a multiple commands
     * with no parent groups required.
     */
    @Test
    public void testGroupAccumulateNoParent() {

        final var command1 = groupTestCommand( group1, true );
        final var command2 = groupTestCommand( group2, true );
        final var command3 = groupTestCommand( group3, false );

        final var chain = List.of( command1, command2, command3 );

        assertThat( InvocationUtils.accumulateGroups( chain ) )
                .containsExactly( group3 );

    }

    /**
     * Tests {@link InvocationUtils#accumulateGroups(List)} with a multiple commands
     * with one parent group required.
     */
    @Test
    public void testGroupAccumulateOneParent() {

        final var command1 = groupTestCommand( group1, true );
        final var command2 = groupTestCommand( group2, false );
        final var command3 = groupTestCommand( group3, true );

        final var chain = List.of( command1, command2, command3 );

        assertThat( InvocationUtils.accumulateGroups( chain ) )
                .containsExactly( group2, group3 );

    }

    /**
     * Tests {@link InvocationUtils#accumulateGroups(List)} with a multiple commands
     * with two parent groups required.
     */
    @Test
    public void testGroupAccumulateTwoParent() {

        final var command1 = groupTestCommand( group1, false );
        final var command2 = groupTestCommand( group2, true );
        final var command3 = groupTestCommand( group3, true );

        final var chain = List.of( command1, command2, command3 );

        assertThat( InvocationUtils.accumulateGroups( chain ) )
                .containsExactly( group1, group2, group3 );

    }

    /**
     * Tests {@link InvocationUtils#accumulateGroups(List)} with a multiple commands
     * with all parent groups required, including at the root command.
     */
    @Test
    public void testGroupAccumulateAllParent() {

        final var command1 = groupTestCommand( group1, true );
        final var command2 = groupTestCommand( group2, true );
        final var command3 = groupTestCommand( group3, true );

        final var chain = List.of( command1, command2, command3 );

        assertThat( InvocationUtils.accumulateGroups( chain ) )
                .containsExactly( group1, group2, group3 );

    }
    
}
