package ie.universityofgalway.groupnine.infrastructure.email.serialization.codec;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ie.universityofgalway.groupnine.domain.email.EmailAddress;
import ie.universityofgalway.groupnine.domain.email.EmailJobId;
import ie.universityofgalway.groupnine.domain.email.EmailType;
import ie.universityofgalway.groupnine.domain.email.Priority;
import ie.universityofgalway.groupnine.domain.email.jobs.PaymentFailedEmailJob;
import ie.universityofgalway.groupnine.service.email.serialization.EmailJobCodec;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

/**
 * Codec for {@link ie.universityofgalway.groupnine.domain.email.jobs.PaymentFailedEmailJob}
 * model payloads used on the queue.
 */
@Component
public class PaymentFailedEmailJobCodec implements EmailJobCodec<PaymentFailedEmailJob> {
    private final ObjectMapper mapper = new ObjectMapper();

    /** @return the email type this codec supports */
    @Override public EmailType type() { return EmailType.PAYMENT_FAILED; }

    /** Serializes the job's model map to a JSON node. */
    @Override public JsonNode writeModel(PaymentFailedEmailJob job) {
        ObjectNode model = mapper.createObjectNode();
        job.templateModel().forEach((k, v) -> model.putPOJO(k, v));
        return model;
    }

    /** Rebuilds the job instance from model JSON and envelope metadata. */
    @Override public PaymentFailedEmailJob readModel(JsonNode modelNode, Envelope env) {
        EmailJobId id = new EmailJobId(UUID.fromString(env.id()));
        EmailAddress to = new EmailAddress(env.to());
        Locale locale = Locale.forLanguageTag(env.locale());
        Priority pr = Priority.valueOf(env.priority());
        Instant createdAt = Instant.parse(env.createdAt());
        PaymentFailedEmailJob.OptionalStep b = PaymentFailedEmailJob.builder().to(to).id(id).locale(locale).priority(pr).createdAt(createdAt);
        if (modelNode.hasNonNull("orderId")) b.orderId(modelNode.path("orderId").asText());
        if (modelNode.hasNonNull("reason")) b.reason(modelNode.path("reason").asText());
        if (modelNode.hasNonNull("userName")) b.userName(modelNode.path("userName").asText());
        return b.build();
    }
}
