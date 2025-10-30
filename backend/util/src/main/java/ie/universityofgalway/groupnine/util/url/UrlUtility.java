package ie.universityofgalway.groupnine.util.url;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Advanced URL/URI utility class.
 *
 * Features:
 * - Safe joining of base + path (without double slashes)
 * - URI normalization (dot segments, lowercase host/scheme, strip default ports)
 * - Query parameter parsing, modification, and encoding
 * - Fragment helpers
 * - Conversion to URL/URI
 * - Fluent Builder for safe URI construction
 */
public final class UrlUtility {

    private UrlUtility() {
        // Utility class, no instances
    }

    // =====================================================
    // Basic Path Join Helpers
    // =====================================================

    /**
     * Joins a base URL/URI string with a path fragment.
     * Examples:
     * join("https://ex.com/api", "/v1") → https://ex.com/api/v1
     * join("https://ex.com/api/", "v1") → https://ex.com/api/v1
     */
    public static String join(String base, String path) {
        Objects.requireNonNull(base, "base");
        Objects.requireNonNull(path, "path");

        // If path looks like an absolute URI, return as is
        try {
            URI p = new URI(path);
            if (p.isAbsolute()) return p.toString();
        } catch (URISyntaxException ignored) {}

        int qIdx = indexOfFirst(base, '?', '#');
        String head = qIdx >= 0 ? base.substring(0, qIdx) : base;
        String tail = qIdx >= 0 ? base.substring(qIdx) : "";

        boolean headEndsSlash = head.endsWith("/");
        boolean pathStartsSlash = path.startsWith("/");

        String joinedPath = headEndsSlash && pathStartsSlash
                ? head + path.substring(1)
                : (!headEndsSlash && !pathStartsSlash ? head + "/" + path : head + path);

        return joinedPath + tail;
    }

    public static String trimTrailingSlash(String s) {
        if (s == null || s.isEmpty()) return s;
        int q = indexOfFirst(s, '?', '#');
        String p = q >= 0 ? s.substring(0, q) : s;
        String rest = q >= 0 ? s.substring(q) : "";
        if (p.length() > 1 && p.endsWith("/")) p = p.substring(0, p.length() - 1);
        return p + rest;
    }

    public static String ensureTrailingSlash(String s) {
        if (s == null || s.isEmpty()) return "/";
        int q = indexOfFirst(s, '?', '#');
        String p = q >= 0 ? s.substring(0, q) : s;
        String rest = q >= 0 ? s.substring(q) : "";
        if (!p.endsWith("/")) p += "/";
        return p + rest;
    }

    // =====================================================
    // Resolution & Normalization
    // =====================================================

    public static String resolve(String base, String ref) {
        try {
            URI b = new URI(base);
            URI r = new URI(ref);
            return b.resolve(r).normalize().toString();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URI(s): " + e.getMessage(), e);
        }
    }

    public static String normalize(String uri) {
        try {
            URI u = new URI(uri).normalize();

            String scheme = lowerOrNull(u.getScheme());
            String userInfo = u.getUserInfo();
            String host = lowerOrNull(u.getHost());
            int port = u.getPort();
            String path = collapsePathSlashes(u.getPath());
            String query = u.getRawQuery();
            String fragment = u.getRawFragment();

            if ((port == 80 && "http".equals(scheme)) || (port == 443 && "https".equals(scheme))) {
                port = -1;
            }

            URI normalized = new URI(scheme, userInfo, host, port, path, query, fragment);
            return normalized.toString();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URI: " + e.getMessage(), e);
        }
    }

    private static String collapsePathSlashes(String path) {
        if (path == null || path.isEmpty()) return path;
        StringBuilder sb = new StringBuilder();
        boolean prevSlash = false;
        for (int i = 0; i < path.length(); i++) {
            char c = path.charAt(i);
            if (c == '/') {
                if (!prevSlash) sb.append(c);
                prevSlash = true;
            } else {
                sb.append(c);
                prevSlash = false;
            }
        }
        return sb.toString();
    }

    // =====================================================
    // Query Parameter Helpers
    // =====================================================

    public static LinkedHashMap<String, List<String>> parseQuery(String uri) {
        try {
            URI u = new URI(uri);
            return parseQueryRaw(u.getRawQuery());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URI: " + e.getMessage(), e);
        }
    }

    public static String getQueryParam(String uri, String name) {
        List<String> values = parseQuery(uri).get(name);
        return (values == null || values.isEmpty()) ? null : values.get(0);
    }

    public static String addQueryParam(String uri, String name, String value) {
        return withQueryMutation(uri, map -> map.computeIfAbsent(name, k -> new ArrayList<>()).add(value));
    }

    public static String setQueryParam(String uri, String name, String value) {
        return withQueryMutation(uri, map -> {
            map.put(name, new ArrayList<>(Collections.singletonList(value)));
        });
    }

    public static String removeQueryParam(String uri, String name) {
        return withQueryMutation(uri, map -> map.remove(name));
    }

    private static String withQueryMutation(String uri, java.util.function.Consumer<LinkedHashMap<String, List<String>>> mut) {
        try {
            URI u = new URI(uri);
            LinkedHashMap<String, List<String>> map = parseQueryRaw(u.getRawQuery());
            mut.accept(map);
            String newQuery = buildQuery(map);
            URI out = new URI(
                    u.getScheme(),
                    u.getUserInfo(),
                    u.getHost(),
                    u.getPort(),
                    u.getPath(),
                    newQuery,
                    u.getRawFragment()
            );
            return out.toString();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URI: " + e.getMessage(), e);
        }
    }

    private static LinkedHashMap<String, List<String>> parseQueryRaw(String raw) {
        LinkedHashMap<String, List<String>> map = new LinkedHashMap<>();
        if (raw == null || raw.isEmpty()) return map;
        String[] pairs = raw.split("&", -1);
        for (String pair : pairs) {
            int idx = pair.indexOf('=');
            String k = idx >= 0 ? pair.substring(0, idx) : pair;
            String v = idx >= 0 ? pair.substring(idx + 1) : "";
            String dk = decodeComponent(k);
            String dv = decodeComponent(v);
            map.computeIfAbsent(dk, x -> new ArrayList<>()).add(dv);
        }
        return map;
    }

    private static String buildQuery(LinkedHashMap<String, List<String>> map) {
        if (map.isEmpty()) return null;
        // Provide an unencoded query string; URI multi-arg constructor will percent-encode as needed.
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, List<String>> e : map.entrySet()) {
            for (String v : e.getValue()) {
                if (!first) sb.append('&');
                sb.append(e.getKey());
                if (v != null && !v.isEmpty()) {
                    sb.append('=');
                    sb.append(v);
                }
                first = false;
            }
        }
        return sb.toString();
    }

    // =====================================================
    // Encoding Helpers
    // =====================================================

    public static String encodeComponent(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (byte b : s.getBytes(StandardCharsets.UTF_8)) {
            char c = (char) (b & 0xFF);
            if (isUnreserved(c)) sb.append(c);
            else {
                sb.append('%');
                String hex = Integer.toHexString(b & 0xFF).toUpperCase(Locale.ROOT);
                if (hex.length() == 1) sb.append('0');
                sb.append(hex);
            }
        }
        return sb.toString();
    }

    public static String decodeComponent(String s) {
        if (s == null) return "";
        int len = s.length();
        byte[] out = new byte[len];
        int oi = 0;
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            if (c == '%') {
                int hi = hex(s.charAt(i + 1));
                int lo = hex(s.charAt(i + 2));
                out[oi++] = (byte) ((hi << 4) + lo);
                i += 2;
            } else {
                out[oi++] = (byte) c;
            }
        }
        return new String(out, 0, oi, StandardCharsets.UTF_8);
    }

    private static boolean isUnreserved(char c) {
        return (c >= 'a' && c <= 'z')
                || (c >= 'A' && c <= 'Z')
                || (c >= '0' && c <= '9')
                || c == '-' || c == '.' || c == '_' || c == '~';
    }

    private static int hex(char c) {
        if (c >= '0' && c <= '9') return c - '0';
        if (c >= 'a' && c <= 'f') return 10 + c - 'a';
        if (c >= 'A' && c <= 'F') return 10 + c - 'A';
        throw new IllegalArgumentException("Invalid hex: " + c);
    }

    // =====================================================
    // Fragments & Conversion
    // =====================================================

    public static String stripFragment(String uri) {
        int i = uri.indexOf('#');
        return i >= 0 ? uri.substring(0, i) : uri;
    }

    public static String setFragment(String uri, String fragment) {
        String noFrag = stripFragment(uri);
        return fragment == null || fragment.isEmpty() ? noFrag : noFrag + "#" + encodeComponent(fragment);
    }

    public static boolean isHttpLike(String uri) {
        try {
            String scheme = new URI(uri).getScheme();
            return "http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme);
        } catch (URISyntaxException e) {
            return false;
        }
    }

    public static URL toURL(String uri) {
        try {
            return new URI(uri).toURL();
        } catch (URISyntaxException | MalformedURLException e) {
            throw new IllegalArgumentException("Invalid URL: " + e.getMessage(), e);
        }
    }

    public static URI toURI(String uri) {
        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URI: " + e.getMessage(), e);
        }
    }

    // =====================================================
    // Fluent Builder
    // =====================================================

    public static final class Builder {
        private String scheme;
        private String host;
        private int port = -1;
        private final List<String> pathSegments = new ArrayList<>();
        private final LinkedHashMap<String, List<String>> query = new LinkedHashMap<>();
        private String fragment;

        public static Builder http(String host) { return new Builder().scheme("http").host(host); }
        public static Builder https(String host) { return new Builder().scheme("https").host(host); }

        public Builder scheme(String scheme) { this.scheme = scheme; return this; }
        public Builder host(String host) { this.host = host; return this; }
        public Builder port(int port) { this.port = port; return this; }
        public Builder fragment(String fragment) { this.fragment = fragment; return this; }

        public Builder addPathSegment(String segment) {
            if (segment != null && !segment.isEmpty()) pathSegments.add(segment);
            return this;
        }

        public Builder queryParam(String name, Object value) {
            query.computeIfAbsent(name, k -> new ArrayList<>()).add(value == null ? "" : value.toString());
            return this;
        }

        public String build() {
            try {
                String path = String.join("/", pathSegments);
                if (!path.isEmpty()) path = "/" + path;
                String q = buildQuery(query);
                URI uri = new URI(scheme, null, host, port, path, q, fragment);
                return uri.toString();
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Invalid URI components: " + e.getMessage(), e);
            }
        }
    }

    // =====================================================
    // Utilities
    // =====================================================

    private static String lowerOrNull(String s) { return s == null ? null : s.toLowerCase(Locale.ROOT); }

    private static int indexOfFirst(String s, char a, char b) {
        int ia = s.indexOf(a);
        int ib = s.indexOf(b);
        if (ia < 0) return ib;
        if (ib < 0) return ia;
        return Math.min(ia, ib);
    }

    /** Backward-compatible with your original simple join. */
    public static String simpleJoin(String base, String path) {
        if (base.endsWith("/") && path.startsWith("/")) return base.substring(0, base.length() - 1) + path;
        if (!base.endsWith("/") && !path.startsWith("/")) return base + "/" + path;
        return base + path;
    }
}
