package ie.universityofgalway.groupnine.delivery.worker.config;

import ie.universityofgalway.groupnine.domain.email.EmailType;
import ie.universityofgalway.groupnine.service.email.port.IdempotencyPort;
import ie.universityofgalway.groupnine.service.email.port.RenderTemplatePort;
import ie.universityofgalway.groupnine.service.email.port.SendEmailPort;
import ie.universityofgalway.groupnine.service.email.usecase.SendEmailUseCase;
import ie.universityofgalway.groupnine.service.mailer.AccountVerificationMailer;
import ie.universityofgalway.groupnine.service.mailer.BaseMailer;
import ie.universityofgalway.groupnine.service.mailer.EmailMailerRegistry;
import ie.universityofgalway.groupnine.service.mailer.WelcomeMailer;
import ie.universityofgalway.groupnine.service.mailer.PasswordResetMailer;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Declares RabbitMQ topology for all {@link ie.universityofgalway.groupnine.domain.email.EmailType}
 * queues (main, retry, DLQ) and wires mailers, registry, and use case beans for the worker.
 * <p>
 * The routing key convention is {@code email.{type}}, main queue is {@code email.{type}.q} with
 * dead-lettering to {@code email.dlx} and retry queues {@code email.{type}.q.retry.{ms}.q}.
 */
@Configuration
@EnableRabbit
@ConditionalOnProperty(prefix = "email.worker", name = "enabled", havingValue = "true", matchIfMissing = true)
public class EmailWorkerConfig {

    public static final String EMAIL_EXCHANGE = "email.exchange";
    public static final String EMAIL_DLX = "email.dlx";

    private static final int[] RETRY_DELAYS_MS = new int[]{60000, 120000, 300000, 600000, 1800000, 3600000};

    public static String routingKeyFor(EmailType type) {
        return "email." + type.name().toLowerCase();
    }

    public static String mainQueueFor(EmailType type) {
        return routingKeyFor(type) + ".q";
    }

    public static String dlqFor(EmailType type) {
        return routingKeyFor(type) + ".dlq";
    }

    @Bean
    public Declarables emailTopology() {
        List<Declarable> declarables = new ArrayList<>();

        TopicExchange exchange = ExchangeBuilder.topicExchange(EMAIL_EXCHANGE).durable(true).build();
        DirectExchange dlx = ExchangeBuilder.directExchange(EMAIL_DLX).durable(true).build();

        declarables.add(exchange);
        declarables.add(dlx);

        for (EmailType type : EmailType.values()) {
            String routingKey = routingKeyFor(type);
            String mainQueue = mainQueueFor(type);
            String dlq = dlqFor(type);

            Map<String, Object> mainArgs = new HashMap<>();
            mainArgs.put("x-dead-letter-exchange", EMAIL_DLX);
            mainArgs.put("x-dead-letter-routing-key", routingKey);
            Queue main = QueueBuilder.durable(mainQueue).withArguments(mainArgs).build();

            Queue deadLetterQ = QueueBuilder.durable(dlq).build();

            declarables.add(main);
            declarables.add(deadLetterQ);
            declarables.add(BindingBuilder.bind(deadLetterQ).to(dlx).with(routingKey));
            declarables.add(BindingBuilder.bind(main).to(exchange).with(routingKey));

            for (int delay : RETRY_DELAYS_MS) {
                Map<String, Object> args = new HashMap<>();
                args.put("x-dead-letter-exchange", EMAIL_EXCHANGE);
                args.put("x-dead-letter-routing-key", routingKey);
                args.put("x-message-ttl", delay);
                String qName = mainQueue + ".retry." + delay + ".q";
                declarables.add(QueueBuilder.durable(qName).withArguments(args).build());
            }
        }
        return new Declarables(declarables);
    }

    @Bean
    public SimpleRabbitListenerContainerFactory emailListenerFactory(ConnectionFactory cf) {
        SimpleRabbitListenerContainerFactory f = new SimpleRabbitListenerContainerFactory();
        f.setConnectionFactory(cf);
        f.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        f.setPrefetchCount(10);
        f.setConcurrentConsumers(2);
        return f;
    }

    @Bean(name = "emailQueueNames")
    public String[] emailQueueNames() {
        EmailType[] types = EmailType.values();
        String[] queues = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            queues[i] = mainQueueFor(types[i]);
        }
        return queues;
    }

    @Bean
    public AccountVerificationMailer accountVerificationMailer(@Autowired RenderTemplatePort renderer,
                                                               @Autowired SendEmailPort sender,
                                                               @Autowired IdempotencyPort idempotency,
                                                               @Value("${mailjet.from.email:no-reply@example.com}") String fromEmail,
                                                               @Value("${mailjet.from.name:GroupNine}") String fromName) {
        return new AccountVerificationMailer(idempotency, renderer, sender, fromEmail, fromName);
    }

    @Bean
    public WelcomeMailer welcomeMailer(@Autowired RenderTemplatePort renderer,
                                       @Autowired SendEmailPort sender,
                                       @Autowired IdempotencyPort idempotency,
                                       @Value("${mailjet.from.email:no-reply@example.com}") String fromEmail,
                                       @Value("${mailjet.from.name:GroupNine}") String fromName) {
        return new WelcomeMailer(idempotency, renderer, sender, fromEmail, fromName);
    }

    @Bean
    public PasswordResetMailer passwordResetMailer(@Autowired RenderTemplatePort renderer,
                                                    @Autowired SendEmailPort sender,
                                                    @Autowired IdempotencyPort idempotency,
                                                    @Value("${mailjet.from.email:no-reply@example.com}") String fromEmail,
                                                    @Value("${mailjet.from.name:GroupNine}") String fromName) {
        return new PasswordResetMailer(idempotency, renderer, sender, fromEmail, fromName);
    }

    @Bean
    public EmailMailerRegistry emailMailerRegistry(List<BaseMailer<?>> mailers) {
        return new EmailMailerRegistry(mailers);
    }

    @Bean
    public SendEmailUseCase sendEmailUseCase(EmailMailerRegistry registry) {
        return new SendEmailUseCase(registry);
    }
}
