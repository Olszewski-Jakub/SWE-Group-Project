package ie.universityofgalway.groupnine.service.payments.webhook.usecase;

import ie.universityofgalway.groupnine.service.audit.port.AuditEventPort;
import ie.universityofgalway.groupnine.service.messaging.port.OutboxPort;
import ie.universityofgalway.groupnine.service.messaging.port.ProcessedEventPort;
import ie.universityofgalway.groupnine.domain.payment.EventEnvelope;
import ie.universityofgalway.groupnine.service.payments.webhook.port.StripeEventParserPort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class ProcessStripeWebhookUseCase {

    private final StripeEventParserPort parser;
    private final ProcessedEventPort processed;
    private final OutboxPort outbox;
    private final AuditEventPort audit;

    public ProcessStripeWebhookUseCase(StripeEventParserPort parser,
                                       ProcessedEventPort processed,
                                       OutboxPort outbox,
                                       AuditEventPort audit) {
        this.parser = Objects.requireNonNull(parser);
        this.processed = Objects.requireNonNull(processed);
        this.outbox = Objects.requireNonNull(outbox);
        this.audit = Objects.requireNonNull(audit);
    }

    /**
     * Parses a Stripe webhook request, enqueues a normalized message for downstream consumers,
     * marks the event as processed (idempotently), and records an audit entry. If the event has
     * already been processed, the call is a no‑op.
     *
     * @param payload   raw request body
     * @param signature value of the {@code Stripe-Signature} header
     * @throws StripeEventParserPort.InvalidSignatureException when signature verification or parsing fails
     */
    public void execute(String payload, String signature) throws StripeEventParserPort.InvalidSignatureException {
        EventEnvelope env = parser.parse(payload, signature);
        String key = env.getStripeEventId();
        if (key != null && processed.alreadyProcessed("stripe", key)) {
            return;
        }

        String routing = "payment.event." + env.getType().replace(':', '.');

        Map<String, Object> headers = new HashMap<>();
        headers.put("stripe_event_id", env.getStripeEventId());
        outbox.enqueue("payments.events", routing, headers, toPayload(env));

        if (key != null) processed.markProcessed("stripe", key);

        assert env.getStripeEventId() != null;
        audit.record(null, "webhook_received", Map.of(
                "stripe_event_id", env.getStripeEventId(),
                "type", env.getType()
        ), Instant.now());
    }

    /**
     * Builds a compact payload map combining the normalized fields and required fallbacks
     * such as {@code stripe_event_id}, {@code type}, and {@code occurred_at}.
     */
    private Map<String, Object> toPayload(EventEnvelope env) {
        Map<String, Object> m = new HashMap<>(env.getNormalizedPayload());
        m.putIfAbsent("stripe_event_id", env.getStripeEventId());
        m.putIfAbsent("type", env.getType());
        m.putIfAbsent("occurred_at", env.getOccurredAt() == null ? Instant.now().toString() : env.getOccurredAt().toString());
        return m;
    }
}
