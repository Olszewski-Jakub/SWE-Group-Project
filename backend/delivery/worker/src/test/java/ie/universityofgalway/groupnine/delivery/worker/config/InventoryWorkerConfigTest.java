package ie.universityofgalway.groupnine.delivery.worker.config;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InventoryWorkerConfigTest {

    @Test
    void topology_contains_exchanges_queues_and_bindings() {
        InventoryWorkerConfig cfg = new InventoryWorkerConfig();
        Declarables d = cfg.inventoryTopology();
        Collection<Declarable> list = d.getDeclarables();

        assertTrue(list.stream().anyMatch(x -> x instanceof Exchange e && e.getName().equals(InventoryWorkerConfig.EX_INVENTORY_COMMANDS)));
        assertTrue(list.stream().anyMatch(x -> x instanceof Exchange e && e.getName().equals(InventoryWorkerConfig.EX_APP_DLX)));

        // queues
        assertTrue(list.stream().anyMatch(x -> x instanceof Queue q && q.getName().equals(InventoryWorkerConfig.Q_INVENTORY_RESERVE)));
        assertTrue(list.stream().anyMatch(x -> x instanceof Queue q && q.getName().equals(InventoryWorkerConfig.Q_INVENTORY_CONFIRM)));
        assertTrue(list.stream().anyMatch(x -> x instanceof Queue q && q.getName().equals(InventoryWorkerConfig.Q_INVENTORY_RELEASE)));

        // binding for release routing key
        assertTrue(list.stream().anyMatch(x -> x instanceof Binding b && "inventory.release.request".equals(b.getRoutingKey())));
    }
}

