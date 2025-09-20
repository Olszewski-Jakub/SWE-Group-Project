package ie.universityofgalway.groupnine.domain.common;

import java.time.Instant;

public interface DomainEvent {
    Instant occurredAt();
}
