package dev.sympho.modular_commands.utils.parse;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Stream;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.sympho.modular_commands.api.command.context.CommandContext;
import dev.sympho.modular_commands.api.command.parameter.parse.ListParser;
import dev.sympho.modular_commands.api.command.parameter.parse.ParserFunction;
import dev.sympho.modular_commands.api.command.parameter.parse.Parsers;
import discord4j.common.util.Snowflake;
import reactor.test.StepVerifier;

/**
 * Test driver for {@link ListParser}.
 *
 * @version 1.0
 * @since 1.0
 */
@ExtendWith( MockitoExtension.class )
public abstract class ListParserTest {

    /** The context to test with. */
    @Mock private CommandContext context;

    /* Test parameters */

    /**
     * Creates test cases with strings.
     *
     * @return The test cases.
     */
    private static Stream<Arguments> stringTestCases() {

        return Stream.of(
            Arguments.of( "one two three", List.of( "one", "two", "three" ) ),
            Arguments.of( "value", List.of( "value" ) ),
            Arguments.of( "", List.of() )
        );

    }

    /**
     * Creates test cases with ints.
     *
     * @return The test cases.
     */
    private static Stream<Arguments> intTestCases() {

        return Stream.of(
            Arguments.of( "1 2 3 4 5", List.of( 1, 2, 3, 4, 5 ) ),
            Arguments.of( "345 94 242", List.of( 345, 94, 242 ) )
        );

    }

    /**
     * Creates test cases with floats.
     *
     * @return The test cases.
     */
    private static Stream<Arguments> floatTestCases() {

        return Stream.of(
            Arguments.of( "1.0 2.0 3.0 4 5.0", List.of( 1.0, 2.0, 3.0, 4.0, 5.0 ) ),
            Arguments.of( "345.45 94.00 0.242", List.of( 345.45, 94.00, 0.242 ) )
        );

    }

    /**
     * Creates test cases with snowflakes.
     *
     * @return The test cases.
     */
    private static Stream<Arguments> snowflakeTestCases() {

        return Stream.of(
            Arguments.of( "531531 35443 343121", List.of( 
                    Snowflake.of( "531531" ), Snowflake.of( "35443" ), Snowflake.of( "343121" ) 
            ) ),
            Arguments.of( "315351", List.of( Snowflake.of( "315351" ) ) )
        );

    }

    /* Generator methods */

    /**
     * Creates a parser.
     *
     * @param <T> The item type.
     * @param itemParser The item parser.
     * @return The parser.
     */
    protected <T extends @NonNull Object> ListParser<T> makeParser( 
            final ParserFunction<String, T> itemParser ) {

        return Parsers.list( itemParser );
            
    }

    /* Test methods */

    /**
     * Test with string items.
     *
     * @param raw The raw value.
     * @param expected The expected result.
     */
    @ParameterizedTest
    @MethodSource( "stringTestCases" )
    public void testPassthrough( final String raw, final List<String> expected ) {

        final var parser = makeParser( Parsers::raw );
        StepVerifier.create( parser.parse( context, raw ) )
                .assertNext( v -> assertThat( v ).isEqualTo( expected ) )
                .verifyComplete();

    }

    /**
     * Test with int items.
     *
     * @param raw The raw value.
     * @param expected The expected result.
     */
    @ParameterizedTest
    @MethodSource( "intTestCases" )
    public void testIntegers( final String raw, final List<Integer> expected ) {

        final var parser = makeParser( Parsers.simple( Integer::valueOf ) );
        StepVerifier.create( parser.parse( context, raw ) )
                .assertNext( v -> assertThat( v ).isEqualTo( expected ) )
                .verifyComplete();

    }

    /**
     * Test with float items.
     *
     * @param raw The raw value.
     * @param expected The expected result.
     */
    @ParameterizedTest
    @MethodSource( "floatTestCases" )
    public void testFloats( final String raw, final List<Double> expected ) {

        final var parser = makeParser( Parsers.simple( Double::valueOf ) );
        StepVerifier.create( parser.parse( context, raw ) )
                .assertNext( v -> assertThat( v ).isEqualTo( expected ) )
                .verifyComplete();

    }

    /**
     * Test with snowflake items.
     *
     * @param raw The raw value.
     * @param expected The expected result.
     */
    @ParameterizedTest
    @MethodSource( "snowflakeTestCases" )
    public void testSnowflakes( final String raw, final List<Snowflake> expected ) {

        final var parser = makeParser( Parsers.simple( Snowflake::of ) );
        StepVerifier.create( parser.parse( context, raw ) )
                .assertNext( v -> assertThat( v ).isEqualTo( expected ) )
                .verifyComplete();

    }
    
}
