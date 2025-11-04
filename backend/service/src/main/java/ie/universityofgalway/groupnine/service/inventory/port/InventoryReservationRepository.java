package ie.universityofgalway.groupnine.service.inventory.port;

import ie.universityofgalway.groupnine.domain.inventory.InventoryReservation;
import ie.universityofgalway.groupnine.domain.inventory.InventoryReservationId;
import ie.universityofgalway.groupnine.domain.order.OrderId;

import java.util.Optional;

/**
 * Repository port for inventory reservations.
 */
public interface InventoryReservationRepository {
    InventoryReservation save(InventoryReservation reservation);
    Optional<InventoryReservation> findById(InventoryReservationId id);
    Optional<InventoryReservation> findByOrderId(OrderId orderId);
}

