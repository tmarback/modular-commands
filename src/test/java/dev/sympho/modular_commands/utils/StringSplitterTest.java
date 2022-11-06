package dev.sympho.modular_commands.utils;

import static org.assertj.core.api.Assertions.*;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import reactor.test.StepVerifier;

/**
 * Test driver for {@link StringSplitter}.
 *
 * @version 1.0
 * @since 1.0
 */
public class StringSplitterTest {

    /**
     * Test driver for {@link StringSplitter.Shell}.
     *
     * @since 1.0
     */
    @Nested
    public class ShellTest {

        /** The instance being tested. */
        public static final StringSplitter.Shell dut = new StringSplitter.Shell();

        /**
         * Tests synchronously parsing an empty string.
         */
        @Test
        public void testSyncEmptyString() {

            assertThat( dut.split( "" ) )
                    .isEmpty();

        }

        /**
         * Tests synchronously parsing a string with one element.
         */
        @Test
        public void testSyncOneElement() {

            assertThat( dut.split( "test" ) )
                    .containsExactly( "test" );

        }

        /**
         * Tests synchronously parsing a string with two elements.
         */
        @Test
        public void testSyncTwoElements() {

            assertThat( dut.split( "test value" ) )
                    .containsExactly( "test", "value" );

        }

        /**
         * Tests synchronously parsing a string with three elements.
         */
        @Test
        public void testSyncThreeElements() {

            assertThat( dut.split( "test value now" ) )
                    .containsExactly( "test", "value", "now" );

        }

        /**
         * Tests synchronously parsing a string with extra spaces.
         */
        @Test
        public void testSyncExtraSpaces() {

            assertThat( dut.split( "  test   value   now    " ) )
                    .containsExactly( "test", "value", "now" );

        }

        /**
         * Tests asynchronously parsing an empty string.
         */
        @Test
        public void testAsyncEmptyString() {

            StepVerifier.create( dut.splitAsync( "" ) )
                    .verifyComplete();

        }

        /**
         * Tests asynchronously parsing a string with one element.
         */
        @Test
        public void testAsyncOneElement() {

            StepVerifier.create( dut.splitAsync( "test" ) )
                    .expectNext( "test" )
                    .verifyComplete();

        }

        /**
         * Tests asynchronously parsing a string with two elements.
         */
        @Test
        public void testAsyncTwoElements() {

            StepVerifier.create( dut.splitAsync( "test value" ) )
                    .expectNext( "test" )
                    .expectNext( "value" )
                    .verifyComplete();

        }

        /**
         * Tests asynchronously parsing a string with three elements.
         */
        @Test
        public void testAsyncThreeElements() {

            StepVerifier.create( dut.splitAsync( "test value now" ) )
                    .expectNext( "test" )
                    .expectNext( "value" )
                    .expectNext( "now" )
                    .verifyComplete();

        }

        /**
         * Tests asynchronously parsing a string with extra spaces.
         */
        @Test
        public void testAsyncExtraSpaces() {

            StepVerifier.create( dut.splitAsync( "  test   value   now    " ) )
                    .expectNext( "test" )
                    .expectNext( "value" )
                    .expectNext( "now" )
                    .verifyComplete();

        }

        /**
         * Test driver for {@link StringSplitter.Shell#iterate(String)}.
         *
         * @since 1.0
         */
        @Nested
        public class IteratorTest {

            /**
             * Test the {@link Iterator#next()} method.
             */
            @Test
            public void testNext() {

                final var it = dut.iterate( "this is a test message" );

                assertThat( it.next() ).isEqualTo( "this" );
                assertThat( it.next() ).isEqualTo( "is" );
                assertThat( it.next() ).isEqualTo( "a" );
                assertThat( it.next() ).isEqualTo( "test" );
                assertThat( it.next() ).isEqualTo( "message" );
                assertThatThrownBy( () -> it.next() ).isInstanceOf( NoSuchElementException.class );

            }

            /**
             * Test the {@link Iterator#hasNext()} method.
             */
            @Test
            public void testHasNext() {

                final var it = dut.iterate( "this is a test message" );
                
                assertThat( it.hasNext() ).isTrue();
                assertThat( it.next() ).isEqualTo( "this" );
                assertThat( it.hasNext() ).isTrue();
                assertThat( it.hasNext() ).isTrue();
                assertThat( it.next() ).isEqualTo( "is" );
                assertThat( it.hasNext() ).isTrue();
                assertThat( it.next() ).isEqualTo( "a" );
                assertThat( it.hasNext() ).isTrue();
                assertThat( it.next() ).isEqualTo( "test" );
                assertThat( it.hasNext() ).isTrue();
                assertThat( it.hasNext() ).isTrue();
                assertThat( it.next() ).isEqualTo( "message" );
                assertThat( it.hasNext() ).isFalse();
                assertThat( it.hasNext() ).isFalse();

            }

            /**
             * Tests the {@link SmartIterator#peek()} method.
             */
            @Test
            public void testPeek() {

                final var it = dut.iterate( "this is a test message" );

                assertThat( it.peek() ).isEqualTo( "this" );
                assertThat( it.peek() ).isEqualTo( "this" );
                assertThat( it.next() ).isEqualTo( "this" );
                assertThat( it.peek() ).isEqualTo( "is" );
                assertThat( it.peek() ).isEqualTo( "is" );
                assertThat( it.next() ).isEqualTo( "is" );
                assertThat( it.peek() ).isEqualTo( "a" );
                assertThat( it.peek() ).isEqualTo( "a" );
                assertThat( it.next() ).isEqualTo( "a" );
                assertThat( it.peek() ).isEqualTo( "test" );
                assertThat( it.peek() ).isEqualTo( "test" );
                assertThat( it.next() ).isEqualTo( "test" );
                assertThat( it.peek() ).isEqualTo( "message" );
                assertThat( it.peek() ).isEqualTo( "message" );
                assertThat( it.next() ).isEqualTo( "message" );
                assertThat( it.peek() ).isNull();
                assertThat( it.peek() ).isNull();

            }

            /**
             * Tests the {@link SmartIterator#toFlux()} method.
             */
            @Test
            public void testFlux() {

                final var it = dut.iterate( "this is a test message" );

                StepVerifier.create( it.toFlux() )
                        .expectNext( "this" )
                        .expectNext( "is" )
                        .expectNext( "a" )
                        .expectNext( "test" )
                        .expectNext( "message" )
                        .verifyComplete();

            }
            
        }

    }
    
}
