package ie.universityofgalway.groupnine.delivery.worker.config;

import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OrdersWorkerConfig {
    public static final String EX_ORDERS_COMMANDS = "orders.commands";

    @Bean
    /**
     * Declares the orders commands exchange used by downstream workers.
     */
    public Declarables ordersTopology() {
        TopicExchange ordersCommands = ExchangeBuilder.topicExchange(EX_ORDERS_COMMANDS).durable(true).build();
        return new Declarables(List.of(ordersCommands));
    }
}
