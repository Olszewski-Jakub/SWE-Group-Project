package ie.universityofgalway.groupnine.infrastructure.messaging.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ie.universityofgalway.groupnine.domain.messaging.OutboxMessage;
import ie.universityofgalway.groupnine.infrastructure.messaging.jpa.OutboxMessageEntity;
import ie.universityofgalway.groupnine.infrastructure.messaging.jpa.OutboxMessageJpaRepository;
import ie.universityofgalway.groupnine.service.messaging.port.OutboxPort;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * JPA implementation of the {@link ie.universityofgalway.groupnine.service.messaging.port.OutboxPort}.
 * Encodes headers and payload to JSON and persists messages for reliable, asynchronous delivery.
 */
@Component
public class OutboxPersistenceAdapter implements OutboxPort {
    private final OutboxMessageJpaRepository repo;
    private final ObjectMapper mapper;

    public OutboxPersistenceAdapter(OutboxMessageJpaRepository repo, ObjectMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    /** Persists a new message to the outbox with JSON-encoded headers and payload. */
    @Override
    public void enqueue(String exchange, String routingKey, Map<String, Object> headers, Object payload) {
        OutboxMessageEntity e = new OutboxMessageEntity();
        e.setId(UUID.randomUUID());
        e.setExchange(exchange);
        e.setRoutingKey(routingKey);
        e.setHeadersJson(toJson(headers == null ? Map.of() : headers));
        e.setPayloadJson(toJson(payload));
        e.setCreatedAt(Instant.now());
        e.setAttempts(0);
        repo.save(e);
    }

    /**
     * Returns unpublished messages ordered by creation time and maps them to domain objects
     * for processing by the worker.
     */
    @Override
    public List<OutboxMessage> findUnpublished() {
        List<OutboxMessageEntity> entities = repo.findUnpublished();
        return entities.stream().map(e ->
                new OutboxMessage(
                        e.getId(),
                        e.getExchange(),
                        e.getRoutingKey(),
                        e.getHeadersJson(),
                        e.getPayloadJson(),
                        e.getCreatedAt(),
                        e.getPublishedAt(),
                        e.getAttempts()
                )
        ).toList();
    }

    /** Updates an existing outbox message (attempts, publishedAt, etc.). */
    @Override
    public void save(OutboxMessage message) {
        repo.save(OutboxMessageEntity.of(
                message.getId(),
                message.getExchange(),
                message.getRoutingKey(),
                message.getHeadersJson(),
                message.getPayloadJson(),
                message.getCreatedAt(),
                message.getPublishedAt(),
                message.getAttempts()
        ));
    }

    private String toJson(Object o) {
        try {
            return mapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }
}
