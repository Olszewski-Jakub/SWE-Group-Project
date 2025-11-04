package ie.universityofgalway.groupnine.infrastructure.messaging.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import ie.universityofgalway.groupnine.domain.messaging.OutboxMessage;
import ie.universityofgalway.groupnine.infrastructure.messaging.jpa.OutboxMessageEntity;
import ie.universityofgalway.groupnine.infrastructure.messaging.jpa.OutboxMessageJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OutboxPersistenceAdapterTest {

    private OutboxMessageJpaRepository repo;
    private ObjectMapper mapper;
    private OutboxPersistenceAdapter adapter;

    @BeforeEach
    void setup() {
        repo = Mockito.mock(OutboxMessageJpaRepository.class);
        mapper = new ObjectMapper();
        adapter = new OutboxPersistenceAdapter(repo, mapper);
    }

    @Test
    void enqueue_serializes_and_saves_entity() {
        Map<String, Object> headers = Map.of("correlation_id", "abc");
        Map<String, Object> payload = Map.of("x", 1);

        adapter.enqueue("ex", "rk", headers, payload);

        ArgumentCaptor<OutboxMessageEntity> cap = ArgumentCaptor.forClass(OutboxMessageEntity.class);
        verify(repo).save(cap.capture());
        OutboxMessageEntity e = cap.getValue();
        assertEquals("ex", e.getExchange());
        assertEquals("rk", e.getRoutingKey());
        assertNotNull(e.getId());
        assertTrue(e.getHeadersJson().contains("correlation_id"));
        assertTrue(e.getPayloadJson().contains("\"x\":1"));
        assertNotNull(e.getCreatedAt());
        assertEquals(0, e.getAttempts());
    }

    @Test
    void findUnpublished_maps_entities_to_domain() {
        OutboxMessageEntity e = new OutboxMessageEntity();
        e.setId(UUID.randomUUID());
        e.setExchange("ex");
        e.setRoutingKey("rk");
        e.setHeadersJson("{\"k\":\"v\"}");
        e.setPayloadJson("{\"p\":1}");
        e.setCreatedAt(Instant.parse("2024-01-01T00:00:00Z"));
        e.setPublishedAt(null);
        e.setAttempts(2);
        when(repo.findUnpublished()).thenReturn(List.of(e));

        List<OutboxMessage> rs = adapter.findUnpublished();
        assertEquals(1, rs.size());
        OutboxMessage m = rs.get(0);
        assertEquals(e.getId(), m.getId());
        assertEquals("ex", m.getExchange());
        assertEquals("rk", m.getRoutingKey());
        assertEquals("{\"k\":\"v\"}", m.getHeadersJson());
        assertEquals("{\"p\":1}", m.getPayloadJson());
        assertEquals(e.getCreatedAt(), m.getCreatedAt());
        assertNull(m.getPublishedAt());
        assertEquals(2, m.getAttempts());
    }

    @Test
    void save_maps_domain_to_entity() {
        OutboxMessage msg = new OutboxMessage(
                UUID.randomUUID(),
                "ex", "rk",
                "{\"h\":1}",
                "{\"p\":2}",
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T01:00:00Z"),
                3
        );

        adapter.save(msg);
        ArgumentCaptor<OutboxMessageEntity> cap = ArgumentCaptor.forClass(OutboxMessageEntity.class);
        verify(repo).save(cap.capture());
        OutboxMessageEntity e = cap.getValue();
        assertEquals(msg.getId(), e.getId());
        assertEquals("ex", e.getExchange());
        assertEquals("rk", e.getRoutingKey());
        assertEquals("{\"h\":1}", e.getHeadersJson());
        assertEquals("{\"p\":2}", e.getPayloadJson());
        assertEquals(msg.getCreatedAt(), e.getCreatedAt());
        assertEquals(msg.getPublishedAt(), e.getPublishedAt());
        assertEquals(3, e.getAttempts());
    }
}

