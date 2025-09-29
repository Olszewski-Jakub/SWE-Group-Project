package ie.universityofgalway.groupnine.delivery.worker.listener;

import com.rabbitmq.client.Channel;
import ie.universityofgalway.groupnine.domain.email.jobs.EmailJob;
import ie.universityofgalway.groupnine.service.email.serialization.EmailJobSerializer;
import ie.universityofgalway.groupnine.service.email.usecase.SendEmailUseCase;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * RabbitMQ consumer for all email queues. It deserializes an {@link EmailJob} from the message body
 * using {@link EmailJobSerializer}, dispatches it to {@link SendEmailUseCase}, and performs manual
 * acknowledgements, with dead-letter and delayed-retry handling based on headers and queue naming.
 */
@Component
@ConditionalOnProperty(prefix = "email.worker", name = "enabled", havingValue = "true", matchIfMissing = true)
public class EmailJobListener {
    private final SendEmailUseCase sendEmail;
    private final EmailJobSerializer serializer;
    private final RabbitTemplate rabbitTemplate;

    public EmailJobListener(SendEmailUseCase sendEmail, EmailJobSerializer serializer, RabbitTemplate rabbitTemplate) {
        this.sendEmail = sendEmail;
        this.serializer = serializer;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = "#{emailQueueNames}", containerFactory = "emailListenerFactory")
    public void onMessage(Message message, Channel channel) throws IOException {
        long tag = message.getMessageProperties().getDeliveryTag();
        Map<String, Object> headers = message.getMessageProperties().getHeaders();
        int attempt = ((Number) headers.getOrDefault("x-attempt", 0)).intValue();
        try {
            EmailJob job = serializer.fromBytes(message.getBody());
            sendEmail.execute(job);
            channel.basicAck(tag, false);
        } catch (Exception ex) {
            int nextDelay = nextDelayMs(attempt + 1);
            if (nextDelay > 0) {
                // publish to delay queue then ack original; delay queue derived from the consumer queue
                String consumerQueue = message.getMessageProperties().getConsumerQueue();
                String delayQueue = consumerQueue + ".retry." + nextDelay + ".q";
                org.springframework.amqp.core.MessageProperties props = new org.springframework.amqp.core.MessageProperties();
                props.setContentType("application/json");
                props.setHeader("x-attempt", attempt + 1);
                org.springframework.amqp.core.Message retryMsg = new org.springframework.amqp.core.Message(message.getBody(), props);
                // default exchange with routingKey = queue name
                rabbitTemplate.send("", delayQueue, retryMsg);
                channel.basicAck(tag, false);
            } else {
                // send to DLQ by rejecting without requeue
                channel.basicReject(tag, false);
            }
        }
    }

    private int nextDelayMs(int attempt) {
        return switch (attempt) {
            case 1 -> 60000;      // 1m
            case 2 -> 120000;     // 2m
            case 3 -> 300000;     // 5m
            case 4 -> 600000;     // 10m
            case 5 -> 1800000;    // 30m
            case 6 -> 3600000;    // 60m
            default -> -1;
        };
    }
}
