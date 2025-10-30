package ie.universityofgalway.groupnine.delivery.worker.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import ie.universityofgalway.groupnine.domain.messaging.OutboxMessage;
import ie.universityofgalway.groupnine.infrastructure.messaging.jpa.OutboxMessageEntity;
import ie.universityofgalway.groupnine.service.messaging.port.OutboxPort;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Component
public class OutboxPublisher {
    private final OutboxPort port;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper mapper;

    public OutboxPublisher(OutboxPort port, RabbitTemplate rabbitTemplate, ObjectMapper mapper) {
        this.port = port;
        this.rabbitTemplate = rabbitTemplate;
        this.mapper = mapper;
    }

    /**
     * Polls unpublished outbox messages, converts them to AMQP messages and sends them
     * to the configured exchange/routing key. Marks messages as published on success,
     * otherwise increments the attempts counter so retry/monitoring can react.
     */
    @Scheduled(fixedDelayString = "${app.outbox.poll-interval-ms:1000}")
    public void publish() {
        List<OutboxMessage> batch = port.findUnpublished();
        for (OutboxMessage e : batch) {
            try {
                byte[] body = e.getPayloadJson().getBytes(StandardCharsets.UTF_8);
                MessageProperties props = new MessageProperties();
                props.setContentType("application/json");
                if (e.getHeadersJson() != null) {
                    Map<String, Object> headers = mapper.readValue(e.getHeadersJson(), Map.class);
                    headers.forEach(props::setHeader);
                }
                Message msg = new Message(body, props);
                rabbitTemplate.send(e.getExchange(), e.getRoutingKey(), msg);
                e.setPublishedAt(Instant.now());
            } catch (Exception ex) {
                e.setAttempts(e.getAttempts() + 1);
            }
            port.save(e);
        }
    }
}
