package ie.universityofgalway.groupnine.infrastructure.email.serialization.codec;

import com.fasterxml.jackson.databind.JsonNode;
import ie.universityofgalway.groupnine.domain.email.EmailAddress;
import ie.universityofgalway.groupnine.domain.email.EmailJobId;
import ie.universityofgalway.groupnine.domain.email.jobs.OrderRefundedEmailJob;
import ie.universityofgalway.groupnine.service.email.serialization.EmailJobCodec;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class OrderRefundedEmailJobCodecTest {

    @Test
    void write_and_read_roundtrip() {
        OrderRefundedEmailJob job = OrderRefundedEmailJob.builder()
                .to(new EmailAddress("user@example.com"))
                .orderId("o2").amountMinor(555).currency("USD").userName("Bob")
                .build();

        OrderRefundedEmailJobCodec codec = new OrderRefundedEmailJobCodec();
        JsonNode node = codec.writeModel(job);
        assertEquals("o2", node.get("orderId").asText());
        assertEquals(555L, node.get("amountMinor").asLong());
        assertEquals("USD", node.get("currency").asText());
        assertEquals("Bob", node.get("userName").asText());

        EmailJobCodec.Envelope env = new EmailJobCodec.Envelope(
                EmailJobId.newId().value().toString(),
                job.to().value(),
                Locale.ENGLISH.toLanguageTag(),
                ie.universityofgalway.groupnine.domain.email.Priority.NORMAL.name(),
                Instant.parse("2024-01-01T00:00:00Z").toString()
        );
        OrderRefundedEmailJob decoded = codec.readModel(node, env);
        assertEquals("user@example.com", decoded.to().value());
        assertEquals("o2", decoded.templateModel().get("orderId"));
        assertEquals(555L, decoded.templateModel().get("amountMinor"));
        assertEquals("USD", decoded.templateModel().get("currency"));
        assertEquals("Bob", decoded.templateModel().get("userName"));
    }
}

