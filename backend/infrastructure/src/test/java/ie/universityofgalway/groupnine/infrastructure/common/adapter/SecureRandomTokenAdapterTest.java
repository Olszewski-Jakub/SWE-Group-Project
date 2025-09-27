package ie.universityofgalway.groupnine.infrastructure.common.adapter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SecureRandomTokenAdapterTest {
    @Test
    void generatesOpaqueAndHash() {
        SecureRandomTokenAdapter adapter = new SecureRandomTokenAdapter();
        String tok = adapter.generateOpaqueToken();
        assertNotNull(tok);
        assertTrue(tok.length() >= 43); // base64url of 32 bytes without padding
        String hash = adapter.sha256(tok);
        assertNotNull(hash);
        assertTrue(hash.length() >= 43);
        assertNotEquals(tok, hash);
    }
}

