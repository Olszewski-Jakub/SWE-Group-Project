package ie.universityofgalway.groupnine.infrastructure.email.serialization.codec;

import com.fasterxml.jackson.databind.JsonNode;
import ie.universityofgalway.groupnine.domain.email.EmailAddress;
import ie.universityofgalway.groupnine.domain.email.EmailJobId;
import ie.universityofgalway.groupnine.domain.email.jobs.PaymentFailedEmailJob;
import ie.universityofgalway.groupnine.service.email.serialization.EmailJobCodec;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class PaymentFailedEmailJobCodecTest {

    @Test
    void write_and_read_roundtrip() {
        PaymentFailedEmailJob job = PaymentFailedEmailJob.builder()
                .to(new EmailAddress("user@example.com"))
                .orderId("o3").reason("PAYMENT_FAILED").userName("Carol")
                .build();

        PaymentFailedEmailJobCodec codec = new PaymentFailedEmailJobCodec();
        JsonNode node = codec.writeModel(job);
        assertEquals("o3", node.get("orderId").asText());
        assertEquals("PAYMENT_FAILED", node.get("reason").asText());
        assertEquals("Carol", node.get("userName").asText());

        EmailJobCodec.Envelope env = new EmailJobCodec.Envelope(
                EmailJobId.newId().value().toString(),
                job.to().value(),
                Locale.ENGLISH.toLanguageTag(),
                ie.universityofgalway.groupnine.domain.email.Priority.NORMAL.name(),
                Instant.parse("2024-01-01T00:00:00Z").toString()
        );
        PaymentFailedEmailJob decoded = codec.readModel(node, env);
        assertEquals("user@example.com", decoded.to().value());
        assertEquals("o3", decoded.templateModel().get("orderId"));
        assertEquals("PAYMENT_FAILED", decoded.templateModel().get("reason"));
        assertEquals("Carol", decoded.templateModel().get("userName"));
    }
}

