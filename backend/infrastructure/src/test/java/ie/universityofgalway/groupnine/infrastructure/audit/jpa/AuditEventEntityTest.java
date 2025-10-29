package ie.universityofgalway.groupnine.infrastructure.audit.jpa;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AuditEventEntityTest {
    @Test
    void gettersSetters() {
        AuditEventEntity e = new AuditEventEntity();
        UUID id = UUID.randomUUID();
        UUID uid = UUID.randomUUID();
        Instant now = Instant.now();
        e.setId(id);
        e.setUserId(uid);
        e.setEventType("LOGIN");
        e.setMetadataJson("{\"ip\":\"127.0.0.1\"}");
        e.setCreatedAt(now);

        assertEquals(id, e.getId());
        assertEquals(uid, e.getUserId());
        assertEquals("LOGIN", e.getEventType());
        assertTrue(e.getMetadataJson().contains("ip"));
        assertEquals(now, e.getCreatedAt());
    }
}

