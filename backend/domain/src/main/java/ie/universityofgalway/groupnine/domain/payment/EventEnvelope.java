package ie.universityofgalway.groupnine.domain.payment;

import java.time.Instant;
import java.util.Map;

public final class EventEnvelope {
    private final String stripeEventId;
    private final String type;
    private final Instant occurredAt;
    private final Map<String, Object> normalizedPayload;

    public EventEnvelope(String stripeEventId, String type, Instant occurredAt, Map<String, Object> normalizedPayload) {
        this.stripeEventId = stripeEventId;
        this.type = type;
        this.occurredAt = occurredAt;
        this.normalizedPayload = normalizedPayload;
    }

    public String getStripeEventId() { return stripeEventId; }
    public String getType() { return type; }
    public Instant getOccurredAt() { return occurredAt; }
    public Map<String, Object> getNormalizedPayload() { return normalizedPayload; }
}

