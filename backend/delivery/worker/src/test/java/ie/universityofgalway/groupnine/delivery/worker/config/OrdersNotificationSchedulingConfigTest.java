package ie.universityofgalway.groupnine.delivery.worker.config;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Exchange;

import static org.junit.jupiter.api.Assertions.*;

class OrdersNotificationSchedulingConfigTest {

    @Test
    void orders_exchange_declared() {
        OrdersWorkerConfig cfg = new OrdersWorkerConfig();
        Declarables d = cfg.ordersTopology();
        assertTrue(d.getDeclarables().stream().anyMatch(x -> x instanceof Exchange e && e.getName().equals(OrdersWorkerConfig.EX_ORDERS_COMMANDS)));
    }

    @Test
    void notifications_exchange_declared() {
        NotificationWorkerConfig cfg = new NotificationWorkerConfig();
        Declarables d = cfg.notificationsTopology();
        assertTrue(d.getDeclarables().stream().anyMatch(x -> x instanceof Exchange e && e.getName().equals(NotificationWorkerConfig.EX_NOTIFICATIONS_EVENTS)));
    }

    @Test
    void scheduling_config_instantiates() {
        // No behavior; just ensure annotated config is instantiable
        assertNotNull(new SchedulingConfig());
    }
}

