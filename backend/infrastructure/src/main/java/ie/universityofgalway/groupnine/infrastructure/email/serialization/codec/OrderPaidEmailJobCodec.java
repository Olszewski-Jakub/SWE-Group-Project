package ie.universityofgalway.groupnine.infrastructure.email.serialization.codec;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ie.universityofgalway.groupnine.domain.email.EmailAddress;
import ie.universityofgalway.groupnine.domain.email.EmailJobId;
import ie.universityofgalway.groupnine.domain.email.EmailType;
import ie.universityofgalway.groupnine.domain.email.Priority;
import ie.universityofgalway.groupnine.domain.email.jobs.OrderPaidEmailJob;
import ie.universityofgalway.groupnine.service.email.serialization.EmailJobCodec;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

/**
 * Codec for {@link ie.universityofgalway.groupnine.domain.email.jobs.OrderPaidEmailJob} model
 * payloads. Translates between the job's template model and a compact JSON object used on the
 * queue, while common envelope fields are handled by the composite serializer.
 */
@Component
public class OrderPaidEmailJobCodec implements EmailJobCodec<OrderPaidEmailJob> {
    private final ObjectMapper mapper = new ObjectMapper();

    /** @return the email type this codec supports */
    @Override
    public EmailType type() { return EmailType.ORDER_PAID; }

    /**
     * Serializes the job's model map into a JSON node.
     */
    @Override
    public JsonNode writeModel(OrderPaidEmailJob job) {
        ObjectNode model = mapper.createObjectNode();
        job.templateModel().forEach((k, v) -> model.putPOJO(k, v));
        return model;
    }

    /**
     * Reconstructs a job instance from its model node and the common envelope metadata.
     */
    @Override
    public OrderPaidEmailJob readModel(JsonNode modelNode, Envelope env) {
        EmailJobId id = new EmailJobId(UUID.fromString(env.id()));
        EmailAddress to = new EmailAddress(env.to());
        Locale locale = Locale.forLanguageTag(env.locale());
        Priority pr = Priority.valueOf(env.priority());
        Instant createdAt = Instant.parse(env.createdAt());
        OrderPaidEmailJob.OptionalStep b = OrderPaidEmailJob.builder().to(to).id(id).locale(locale).priority(pr).createdAt(createdAt);
        if (modelNode.hasNonNull("orderId")) b.orderId(modelNode.path("orderId").asText());
        if (modelNode.hasNonNull("amountMinor")) b.amountMinor(modelNode.path("amountMinor").asLong());
        if (modelNode.hasNonNull("currency")) b.currency(modelNode.path("currency").asText());
        if (modelNode.hasNonNull("userName")) b.userName(modelNode.path("userName").asText());
        return b.build();
    }
}
