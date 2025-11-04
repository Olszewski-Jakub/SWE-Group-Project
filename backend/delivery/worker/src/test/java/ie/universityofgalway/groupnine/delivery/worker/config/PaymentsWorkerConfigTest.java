package ie.universityofgalway.groupnine.delivery.worker.config;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;

import static org.junit.jupiter.api.Assertions.*;

class PaymentsWorkerConfigTest {
    @Test
    void topology_contains_payments_exchange_queue_and_binding() {
        PaymentsWorkerConfig cfg = new PaymentsWorkerConfig();
        Declarables d = cfg.paymentsTopology();
        assertTrue(d.getDeclarables().stream().anyMatch(x -> x instanceof Exchange e && e.getName().equals(PaymentsWorkerConfig.EX_PAYMENTS_EVENTS)));
        assertTrue(d.getDeclarables().stream().anyMatch(x -> x instanceof Queue q && q.getName().equals(PaymentsWorkerConfig.Q_ORDERS_PAYMENT_HANDLER)));
        assertTrue(d.getDeclarables().stream().anyMatch(x -> x instanceof Binding b && "payment.event.#".equals(b.getRoutingKey())));
    }
}

