package ie.universityofgalway.groupnine.infrastructure.auth.adapter;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BCryptPasswordHasherAdapterTest {
    @Test
    void hashesAndMatches() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);
        BCryptPasswordHasherAdapter adapter = new BCryptPasswordHasherAdapter(encoder);
        String hash = adapter.hash("secret-password");
        assertNotNull(hash);
        assertTrue(adapter.matches("secret-password", hash));
        assertFalse(adapter.matches("other", hash));
    }
}

