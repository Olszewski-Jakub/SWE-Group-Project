package ie.universityofgalway.groupnine.infrastructure.messaging.adapter;

import ie.universityofgalway.groupnine.infrastructure.messaging.jpa.ProcessedEventEntity;
import ie.universityofgalway.groupnine.infrastructure.messaging.jpa.ProcessedEventId;
import ie.universityofgalway.groupnine.infrastructure.messaging.jpa.ProcessedEventJpaRepository;
import ie.universityofgalway.groupnine.service.messaging.port.ProcessedEventPort;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * JPA implementation of {@link ie.universityofgalway.groupnine.service.messaging.port.ProcessedEventPort}
 * used for idempotency tracking of external events (e.g., Stripe webhooks, AMQP messages).
 */
@Component
public class ProcessedEventPersistenceAdapter implements ProcessedEventPort {
    private final ProcessedEventJpaRepository repo;

    public ProcessedEventPersistenceAdapter(ProcessedEventJpaRepository repo) { this.repo = repo; }

    /** @return true if the (source, key) tuple was processed before. */
    @Override
    public boolean alreadyProcessed(String source, String key) {
        return repo.existsBySourceAndKey(source, key);
    }

    /** Marks the (source, key) tuple as processed with a timestamp. */
    @Override
    public void markProcessed(String source, String key) {
        ProcessedEventEntity e = new ProcessedEventEntity();
        e.setSource(source); e.setKey(key); e.setProcessedAt(Instant.now());
        repo.save(e);
    }
}
