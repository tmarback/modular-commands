package dev.sympho.modular_commands.utils.parse;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.jupiter.api.Test;

/**
 * Tests for static functions in {@link UrlParser}.
 *
 * @version 1.0
 * @since 1.0
 */
public class UrlParserTest {

    /**
     * Test for {@link UrlParser#stripFormatting(String)}.
     */
    @Test
    public void testStripFormatting() {

        assertThat( UrlParser.stripFormatting( "test" ) ).isEqualTo( "test" );
        assertThat( UrlParser.stripFormatting( "<test>" ) ).isEqualTo( "test" );
        assertThat( UrlParser.stripFormatting( "[test1](test2)" ) ).isEqualTo( "test2" );
        assertThat( UrlParser.stripFormatting( "[test1](<test2>)" ) ).isEqualTo( "test2" );

    }

    /**
     * Test for {@link UrlParser#parseUrl(String, java.util.Collection)}.
     */
    @Test
    public void testParseUrl() throws MalformedURLException {

        assertThat( UrlParser.parseUrl( "test", UrlParserUtils.PROTOCOL_HTTPS ) ).isNull();
        assertThat( UrlParser.parseUrl( "https://test.com", UrlParserUtils.PROTOCOL_HTTPS ) )
                .isEqualTo( new URL( "https://test.com" ) );
        assertThat( UrlParser.parseUrl( "<https://test.com>", UrlParserUtils.PROTOCOL_HTTPS ) )
                .isEqualTo( new URL( "https://test.com" ) );
        assertThat( UrlParser.parseUrl( 
                        "[a link](https://test.com)", 
                        UrlParserUtils.PROTOCOL_HTTPS ) 
                )
                .isEqualTo( new URL( "https://test.com" ) );
        assertThat( UrlParser.parseUrl( "http://test.com", UrlParserUtils.PROTOCOL_HTTPS ) )
                .isNull();
        assertThat( UrlParser.parseUrl( 
                        "http://test.com", 
                        UrlParserUtils.PROTOCOL_HTTP_COMPATIBLE )
                )
                .isEqualTo( new URL( "http://test.com" ) );

    }
    
}
