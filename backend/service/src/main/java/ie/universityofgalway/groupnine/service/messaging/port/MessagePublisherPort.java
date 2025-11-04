package ie.universityofgalway.groupnine.service.messaging.port;

import java.util.Map;

/**
 * Generic message publisher port for RabbitMQ or other brokers.
 */
public interface MessagePublisherPort {
    void publish(String exchange, String routingKey, Map<String, Object> headers, byte[] payload);
}

