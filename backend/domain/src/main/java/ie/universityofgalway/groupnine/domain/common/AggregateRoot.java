package ie.universityofgalway.groupnine.domain.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base class for domain aggregates that can record domain events.
 */
public abstract class AggregateRoot {
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    /**
     * Records a domain event to be dispatched after the transaction.
     */
    protected void recordEvent(DomainEvent event) {
        domainEvents.add(event);
    }

    /**
     * Returns and clears the recorded domain events.
     */
    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> copy = List.copyOf(domainEvents);
        domainEvents.clear();
        return Collections.unmodifiableList(copy);
    }
}
