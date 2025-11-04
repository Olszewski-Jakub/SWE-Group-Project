package ie.universityofgalway.groupnine.delivery.worker.config;

import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class NotificationWorkerConfig {
    public static final String EX_NOTIFICATIONS_EVENTS = "notifications.events";

    @Bean
    /**
     * Declares the notifications topic exchange. Queues/bindings are defined by listeners.
     */
    public Declarables notificationsTopology() {
        TopicExchange notificationsEvents = ExchangeBuilder.topicExchange(EX_NOTIFICATIONS_EVENTS).durable(true).build();
        return new Declarables(List.of(notificationsEvents));
    }
}
