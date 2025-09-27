package ie.universityofgalway.groupnine.service.audit.port;

import ie.universityofgalway.groupnine.domain.user.UserId;

import java.time.Instant;
import java.util.Map;

/**
 * Port to record security/audit events in a durable store or external system.
 * Implementations can write to DB, log, message bus, etc.
 */
public interface AuditEventPort {
    void record(UserId userId, String eventType, Map<String, Object> metadata, Instant createdAt);
}
