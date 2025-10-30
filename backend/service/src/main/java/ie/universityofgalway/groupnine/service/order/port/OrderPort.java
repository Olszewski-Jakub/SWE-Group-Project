package ie.universityofgalway.groupnine.service.order.port;

import ie.universityofgalway.groupnine.domain.order.Order;
import ie.universityofgalway.groupnine.domain.order.OrderId;

import java.util.Optional;

/**
 * Repository port for persisting and loading Orders.
 */
public interface OrderPort {
    Order save(Order order);
    Optional<Order> findById(OrderId id);
}

