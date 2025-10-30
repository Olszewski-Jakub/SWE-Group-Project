package ie.universityofgalway.groupnine.infrastructure.email.serialization.codec;

import com.fasterxml.jackson.databind.JsonNode;
import ie.universityofgalway.groupnine.domain.email.EmailAddress;
import ie.universityofgalway.groupnine.domain.email.EmailJobId;
import ie.universityofgalway.groupnine.domain.email.jobs.OrderPaidEmailJob;
import ie.universityofgalway.groupnine.service.email.serialization.EmailJobCodec;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class OrderPaidEmailJobCodecTest {

    @Test
    void write_and_read_roundtrip() {
        OrderPaidEmailJob job = OrderPaidEmailJob.builder()
                .to(new EmailAddress("user@example.com"))
                .orderId("o1").amountMinor(1234).currency("EUR").userName("Alice")
                .build();

        OrderPaidEmailJobCodec codec = new OrderPaidEmailJobCodec();
        JsonNode node = codec.writeModel(job);
        assertEquals("o1", node.get("orderId").asText());
        assertEquals(1234L, node.get("amountMinor").asLong());
        assertEquals("EUR", node.get("currency").asText());
        assertEquals("Alice", node.get("userName").asText());

        EmailJobCodec.Envelope env = new EmailJobCodec.Envelope(
                EmailJobId.newId().value().toString(),
                job.to().value(),
                Locale.ENGLISH.toLanguageTag(),
                ie.universityofgalway.groupnine.domain.email.Priority.NORMAL.name(),
                Instant.parse("2024-01-01T00:00:00Z").toString()
        );
        OrderPaidEmailJob decoded = codec.readModel(node, env);
        assertEquals("user@example.com", decoded.to().value());
        assertEquals("o1", decoded.templateModel().get("orderId"));
        assertEquals(1234L, decoded.templateModel().get("amountMinor"));
        assertEquals("EUR", decoded.templateModel().get("currency"));
        assertEquals("Alice", decoded.templateModel().get("userName"));
    }
}

