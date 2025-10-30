package ie.universityofgalway.groupnine.infrastructure.messaging.jpa;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProcessedEventIdTest {

    @Test
    void equals_and_hashCode_on_source_and_key() {
        ProcessedEventId a = new ProcessedEventId("stripe", "evt_123");
        ProcessedEventId b = new ProcessedEventId("stripe", "evt_123");
        ProcessedEventId c = new ProcessedEventId("amqp", "evt_123");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
    }
}

