package ie.universityofgalway.groupnine.infrastructure.email;

import ie.universityofgalway.groupnine.domain.email.jobs.EmailJob;
import ie.universityofgalway.groupnine.service.email.port.EnqueueEmailPort;
import ie.universityofgalway.groupnine.service.email.serialization.EmailJobSerializer;
import ie.universityofgalway.groupnine.util.logging.AppLogger;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ adapter that publishes {@link EmailJob} messages to an exchange using a routing key of
 * the form {@code email.{type}}. The message body is provided by {@link EmailJobSerializer}.
 */
@Component
public class RabbitEmailPublisherAdapter implements EnqueueEmailPort {
    private static final AppLogger log = AppLogger.get(RabbitEmailPublisherAdapter.class);
    private final RabbitTemplate rabbitTemplate;
    private final EmailJobSerializer serializer;

    @Autowired
    public RabbitEmailPublisherAdapter(RabbitTemplate rabbitTemplate, EmailJobSerializer serializer) {
        this.rabbitTemplate = rabbitTemplate;
        this.serializer = serializer;
    }

    @Override
    public void enqueue(EmailJob job) {
        byte[] body = serializer.toBytes(job);
        MessageProperties props = new MessageProperties();
        props.setContentType("application/json");
        props.setHeader("x-attempt", 0);
        Message message = new Message(body, props);
        String routingKey = "email." + job.type().name().toLowerCase();
        rabbitTemplate.send("email.exchange", routingKey, message);
        log.info("email_job_enqueued", "type", job.type().name(), "to", job.to().value());
    }
}
