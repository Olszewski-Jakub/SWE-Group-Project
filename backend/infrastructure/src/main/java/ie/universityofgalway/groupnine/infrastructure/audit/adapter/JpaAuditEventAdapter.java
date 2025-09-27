package ie.universityofgalway.groupnine.infrastructure.audit.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ie.universityofgalway.groupnine.domain.user.UserId;
import ie.universityofgalway.groupnine.infrastructure.audit.jpa.AuditEventEntity;
import ie.universityofgalway.groupnine.infrastructure.audit.jpa.AuditEventJpaRepository;
import ie.universityofgalway.groupnine.service.audit.port.AuditEventPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * JPA-based {@link AuditEventPort} implementation persisting events to audit_events with JSONB metadata.
 */
@Component
public class JpaAuditEventAdapter implements AuditEventPort {
    private final AuditEventJpaRepository repo;
    private final ObjectMapper objectMapper;

    @Autowired
    public JpaAuditEventAdapter(AuditEventJpaRepository repo, ObjectMapper objectMapper) {
        this.repo = repo;
        this.objectMapper = objectMapper;
    }

    @Override
    public void record(UserId userId, String eventType, Map<String, Object> metadata, Instant createdAt) {
        AuditEventEntity e = new AuditEventEntity();
        e.setId(UUID.randomUUID());
        e.setUserId(userId == null ? null : userId.value());
        e.setEventType(eventType);
        e.setCreatedAt(createdAt == null ? Instant.now() : createdAt);
        e.setMetadataJson(serialize(metadata));
        repo.save(e);
    }

    private String serialize(Map<String, Object> metadata) {
        try {
            return metadata == null ? "{}" : objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}
