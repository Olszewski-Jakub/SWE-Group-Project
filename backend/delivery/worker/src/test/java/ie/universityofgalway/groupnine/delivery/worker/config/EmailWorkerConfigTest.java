package ie.universityofgalway.groupnine.delivery.worker.config;

import ie.universityofgalway.groupnine.domain.email.EmailType;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EmailWorkerConfigTest {

    @Test
    void routing_and_queue_naming_conventions() {
        assertEquals("email.account_verification", EmailWorkerConfig.routingKeyFor(EmailType.ACCOUNT_VERIFICATION));
        assertEquals("email.order_refunded", EmailWorkerConfig.routingKeyFor(EmailType.ORDER_REFUNDED));
        assertEquals("email.welcome.q", EmailWorkerConfig.mainQueueFor(EmailType.WELCOME));
        assertEquals("email.password_reset.dlq", EmailWorkerConfig.dlqFor(EmailType.PASSWORD_RESET));
    }

    @Test
    void emailQueueNames_contains_main_queues_for_all_types() {
        EmailWorkerConfig cfg = new EmailWorkerConfig();
        String[] queues = cfg.emailQueueNames();
        assertEquals(EmailType.values().length, queues.length);
        for (int i = 0; i < EmailType.values().length; i++) {
            assertEquals(EmailWorkerConfig.mainQueueFor(EmailType.values()[i]), queues[i]);
        }
    }

    @Test
    void emailTopology_declares_exchanges_and_queues() {
        EmailWorkerConfig cfg = new EmailWorkerConfig();
        Declarables decs = cfg.emailTopology();
        Collection<Declarable> list = decs.getDeclarables();

        // Exchanges present
        assertTrue(list.stream().anyMatch(d -> d instanceof Exchange ex && ex.getName().equals(EmailWorkerConfig.EMAIL_EXCHANGE)));
        assertTrue(list.stream().anyMatch(d -> d instanceof Exchange ex && ex.getName().equals(EmailWorkerConfig.EMAIL_DLX)));

        // One representative queue and a retry queue exists
        String sampleMain = EmailWorkerConfig.mainQueueFor(EmailType.ACCOUNT_VERIFICATION);
        assertTrue(list.stream().anyMatch(d -> d instanceof Queue q && q.getName().equals(sampleMain)));
        assertTrue(list.stream().anyMatch(d -> d instanceof Queue q && q.getName().equals(sampleMain + ".retry.60000.q")));
    }
}

