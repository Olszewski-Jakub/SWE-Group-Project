package ie.universityofgalway.groupnine.infrastructure.auth.adapter;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BCryptPasswordHasherAdapterTest {

    @Test
    void hashes_and_matches() {
        BCryptPasswordHasherAdapter adapter = new BCryptPasswordHasherAdapter(new BCryptPasswordEncoder(4));
        String hash = adapter.hash("supersecurepwd");
        assertNotNull(hash);
        assertTrue(adapter.matches("supersecurepwd", hash));
        assertFalse(adapter.matches("wrong", hash));
    }
}

