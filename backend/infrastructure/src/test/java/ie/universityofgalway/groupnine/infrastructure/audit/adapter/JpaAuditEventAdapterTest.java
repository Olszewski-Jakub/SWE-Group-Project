package ie.universityofgalway.groupnine.infrastructure.audit.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import ie.universityofgalway.groupnine.domain.user.UserId;
import ie.universityofgalway.groupnine.infrastructure.audit.jpa.AuditEventEntity;
import ie.universityofgalway.groupnine.infrastructure.audit.jpa.AuditEventJpaRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JpaAuditEventAdapterTest {

    @Test
    void record_serializes_metadata_and_saves() {
        AuditEventJpaRepository repo = mock(AuditEventJpaRepository.class);
        ObjectMapper mapper = new ObjectMapper();
        JpaAuditEventAdapter adapter = new JpaAuditEventAdapter(repo, mapper);

        UserId uid = UserId.of(UUID.randomUUID());
        Instant now = Instant.now();
        adapter.record(uid, "LOGIN", Map.of("ip", "127.0.0.1"), now);

        ArgumentCaptor<AuditEventEntity> cap = ArgumentCaptor.forClass(AuditEventEntity.class);
        verify(repo).save(cap.capture());
        AuditEventEntity e = cap.getValue();
        assertEquals(uid.value(), e.getUserId());
        assertEquals("LOGIN", e.getEventType());
        assertEquals(now, e.getCreatedAt());
        assertTrue(e.getMetadataJson().contains("ip"));
    }

    @Test
    void record_handlesInvalidJsonGracefully() {
        AuditEventJpaRepository repo = mock(AuditEventJpaRepository.class);
        ObjectMapper mapper = mock(ObjectMapper.class);
        // force error on writeValueAsString
        try { when(mapper.writeValueAsString(any())).thenThrow(new com.fasterxml.jackson.core.JsonProcessingException("x"){}); } catch (Exception ignored) {}
        JpaAuditEventAdapter adapter = new JpaAuditEventAdapter(repo, mapper);
        adapter.record(null, "EV", Map.of("k","v"), Instant.now());
        ArgumentCaptor<AuditEventEntity> cap = ArgumentCaptor.forClass(AuditEventEntity.class);
        verify(repo).save(cap.capture());
        assertEquals("{}", cap.getValue().getMetadataJson());
    }
}

