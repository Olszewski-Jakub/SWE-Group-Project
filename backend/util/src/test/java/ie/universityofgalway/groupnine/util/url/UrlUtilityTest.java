package ie.universityofgalway.groupnine.util.url;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UrlUtilityTest {

    @Test
    void join_handles_slashes_and_absolute_ref() {
        assertEquals("https://ex.com/api/v1", UrlUtility.join("https://ex.com/api", "/v1"));
        assertEquals("https://ex.com/api/v1", UrlUtility.join("https://ex.com/api/", "v1"));
        // absolute path wins
        assertEquals("https://a/b", UrlUtility.join("https://ex.com/x", "https://a/b"));
        // preserve query/fragment on base
        assertEquals("https://ex.com/a/b?x=1#f", UrlUtility.join("https://ex.com/a?x=1#f", "b"));
    }

    @Test
    void trailing_slash_helpers() {
        assertEquals("/a/b", UrlUtility.trimTrailingSlash("/a/b/"));
        assertEquals("/a/b?x=1", UrlUtility.trimTrailingSlash("/a/b/?x=1"));
        assertEquals("/a/b/", UrlUtility.ensureTrailingSlash("/a/b"));
        assertEquals("/a/b/?x=1", UrlUtility.ensureTrailingSlash("/a/b?x=1"));
    }

    @Test
    void resolve_and_normalize() {
        assertEquals("https://ex.com/a/b", UrlUtility.resolve("https://ex.com/a/", "b"));
        // collapse dot segments and lower-case scheme/host, strip default ports
        assertEquals("https://ex.com/a/b", UrlUtility.normalize("HTTPS://EX.COM:443/a/./b"));
        assertEquals("http://ex.com/a/b", UrlUtility.normalize("http://EX.com:80/a//b"));
    }

    @Test
    void query_param_helpers() {
        String u = "https://ex.com/a?x=1&y=2&x=3";
        LinkedHashMap<String, List<String>> map = UrlUtility.parseQuery(u);
        assertEquals(List.of("1","3"), map.get("x"));
        assertEquals("2", UrlUtility.getQueryParam(u, "y"));

        String u2 = UrlUtility.addQueryParam(u, "z", "t e s t");
        assertEquals("t e s t", UrlUtility.getQueryParam(u2, "z"));

        String u3 = UrlUtility.setQueryParam(u, "x", "9");
        assertEquals("9", UrlUtility.getQueryParam(u3, "x"));

        String u4 = UrlUtility.removeQueryParam(u3, "y");
        assertNull(UrlUtility.getQueryParam(u4, "y"));
    }

    @Test
    void encoding_and_decoding() {
        String enc = UrlUtility.encodeComponent("a b~");
        assertEquals("a%20b~", enc);
        assertEquals("a b~", UrlUtility.decodeComponent(enc));
    }

    @Test
    void fragments_and_conversion() throws Exception {
        String base = "https://ex.com/a#frag";
        assertEquals("https://ex.com/a", UrlUtility.stripFragment(base));
        assertEquals("https://ex.com/a#note-1", UrlUtility.setFragment("https://ex.com/a", "note-1"));

        assertTrue(UrlUtility.isHttpLike("https://ex.com"));
        assertFalse(UrlUtility.isHttpLike("mailto:test@example.com"));

        URL url = UrlUtility.toURL("https://ex.com/x");
        URI uri = UrlUtility.toURI("https://ex.com/x");
        assertEquals("https://ex.com/x", url.toString());
        assertEquals("https://ex.com/x", uri.toString());
    }

    @Test
    void builder_builds_expected_uri() {
        String s = UrlUtility.Builder.https("example.com")
                .port(443)
                .addPathSegment("api")
                .addPathSegment("v1")
                .queryParam("q", "coffee")
                .fragment("top")
                .build();
        assertEquals("https://example.com:443/api/v1?q=coffee#top", s);
    }

    @Test
    void simpleJoin_preserves_original_logic() {
        assertEquals("a/b", UrlUtility.simpleJoin("a", "b"));
        assertEquals("a/b", UrlUtility.simpleJoin("a/", "b"));
        assertEquals("a/b", UrlUtility.simpleJoin("a", "/b"));
        assertEquals("a/b", UrlUtility.simpleJoin("a/", "/b"));
    }
}
