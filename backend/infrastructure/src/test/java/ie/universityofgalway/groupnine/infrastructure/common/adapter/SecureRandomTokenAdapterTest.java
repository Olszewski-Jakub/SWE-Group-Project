package ie.universityofgalway.groupnine.infrastructure.common.adapter;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecureRandomTokenAdapterTest {

    @Test
    void generates_base64url_token_and_sha256_hash() {
        SecureRandomTokenAdapter adapter = new SecureRandomTokenAdapter();
        String token = adapter.generateOpaqueToken();
        assertNotNull(token);
        assertFalse(token.contains("+"));
        assertFalse(token.contains("/"));
        assertFalse(token.contains("="));
        // 32 bytes -> 43-44 chars base64url (without padding)
        assertTrue(token.length() >= 43 && token.length() <= 44);

        String hash = adapter.sha256("hello");
        assertNotNull(hash);
        assertFalse(hash.contains("="));
        assertTrue(hash.length() >= 43); // 32 bytes -> 43-44 chars
    }

    @Test
    void tokens_are_probably_unique() {
        SecureRandomTokenAdapter adapter = new SecureRandomTokenAdapter();
        Set<String> set = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            assertTrue(set.add(adapter.generateOpaqueToken()));
        }
    }
}

