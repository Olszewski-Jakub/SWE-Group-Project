package ie.universityofgalway.groupnine.delivery.worker.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import ie.universityofgalway.groupnine.domain.messaging.OutboxMessage;
import ie.universityofgalway.groupnine.service.messaging.port.OutboxPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OutboxPublisherTest {

    private OutboxPort port;
    private RabbitTemplate rabbit;
    private ObjectMapper mapper;

    @BeforeEach
    void setup() {
        port = Mockito.mock(OutboxPort.class);
        rabbit = Mockito.mock(RabbitTemplate.class);
        mapper = new ObjectMapper();
    }

    private OutboxMessage msg(String headersJson) {
        return new OutboxMessage(
                UUID.randomUUID(),
                "ex",
                "rk",
                headersJson,
                "{\"x\":1}",
                Instant.now(),
                null,
                0
        );
    }

    @Test
    void publish_sends_and_marks_published() {
        OutboxMessage m = msg("{\"k\":\"v\"}");
        when(port.findUnpublished()).thenReturn(List.of(m));

        OutboxPublisher pub = new OutboxPublisher(port, rabbit, mapper);
        pub.publish();

        ArgumentCaptor<Message> cap = ArgumentCaptor.forClass(Message.class);
        verify(rabbit).send(eq("ex"), eq("rk"), cap.capture());
        assertEquals("application/json", cap.getValue().getMessageProperties().getContentType());
        assertEquals("v", cap.getValue().getMessageProperties().getHeaders().get("k"));
        assertNotNull(m.getPublishedAt());
        verify(port).save(m);
    }

    @Test
    void publish_onError_increments_attempts_and_persists() {
        OutboxMessage m = msg(null);
        when(port.findUnpublished()).thenReturn(List.of(m));
        doThrow(new RuntimeException("boom")).when(rabbit).send(anyString(), anyString(), any(Message.class));

        OutboxPublisher pub = new OutboxPublisher(port, rabbit, mapper);
        pub.publish();

        assertEquals(1, m.getAttempts());
        verify(port).save(m);
    }
}

