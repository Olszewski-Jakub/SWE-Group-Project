package ie.universityofgalway.groupnine.domain.common;

import java.time.Instant;

/**
 * Marker interface for immutable domain events with an occurrence timestamp.
 */
public interface DomainEvent {
    /**
     * Timestamp when the event occurred.
     */
    Instant occurredAt();
}
