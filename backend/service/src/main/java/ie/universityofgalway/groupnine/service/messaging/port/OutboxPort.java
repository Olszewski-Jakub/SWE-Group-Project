package ie.universityofgalway.groupnine.service.messaging.port;

import ie.universityofgalway.groupnine.domain.messaging.OutboxMessage;

import java.util.List;
import java.util.Map;

/**
 * Transactional Outbox port. Implementations must persist the message within the caller's
 * transaction and publish it asynchronously later.
 */
public interface OutboxPort {
    void enqueue(String exchange, String routingKey, Map<String, Object> headers, Object payload);
    List<OutboxMessage> findUnpublished();
    void save(OutboxMessage message);
}

