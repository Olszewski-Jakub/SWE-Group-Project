package ie.universityofgalway.groupnine.infrastructure.auth.jpa;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PasswordResetTokenEntityTest {
    @Test
    void gettersSetters() {
        PasswordResetTokenEntity e = new PasswordResetTokenEntity();
        UUID id = UUID.randomUUID();
        UUID uid = UUID.randomUUID();
        Instant now = Instant.now();
        e.setId(id);
        e.setUserId(uid);
        e.setTokenHash("h");
        e.setExpiresAt(now.plusSeconds(60));
        e.setCreatedAt(now);
        e.setUsedAt(null);
        assertEquals(id, e.getId());
        assertEquals(uid, e.getUserId());
        assertEquals("h", e.getTokenHash());
        assertEquals(now.plusSeconds(60), e.getExpiresAt());
        assertEquals(now, e.getCreatedAt());
        assertNull(e.getUsedAt());
    }
}

