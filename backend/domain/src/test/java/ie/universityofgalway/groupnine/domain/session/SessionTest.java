package ie.universityofgalway.groupnine.domain.session;

import ie.universityofgalway.groupnine.domain.user.UserId;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SessionTest {
    @Test
    void createNew_setsCoreFields() throws Exception {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(3600);
        Session s = Session.createNew(UserId.newId(), "hash", "UA", InetAddress.getByName("127.0.0.1"), now, exp);
        assertNull(s.getId());
        assertEquals("hash", s.getRefreshTokenHash());
        assertEquals("UA", s.getUserAgent());
        assertEquals(exp, s.getExpiresAt());
        assertNull(s.getRevokedAt());
        assertNull(s.getReplacedBySessionId());
    }

    @Test
    void revoke_returns_new_instance_with_reason_and_replacement() throws Exception {
        Instant now = Instant.now();
        Session s = Session.createNew(UserId.newId(), "hash", "UA", InetAddress.getByName("127.0.0.1"), now, now.plusSeconds(10));
        UUID newId = UUID.randomUUID();
        Session revoked = s.revoke(now.plusSeconds(1), "compromised", newId);
        assertNull(s.getRevokedAt());
        assertEquals(now.plusSeconds(1), revoked.getRevokedAt());
        assertEquals("compromised", revoked.getReason());
        assertEquals(newId, revoked.getReplacedBySessionId());
    }
}

