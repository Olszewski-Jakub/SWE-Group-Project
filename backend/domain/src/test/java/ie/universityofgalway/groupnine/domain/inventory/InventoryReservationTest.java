package ie.universityofgalway.groupnine.domain.inventory;

import ie.universityofgalway.groupnine.domain.order.OrderId;
import ie.universityofgalway.groupnine.domain.product.VariantId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InventoryReservationTest {

    private ReservationItem item(int qty) {
        return new ReservationItem(new VariantId(UUID.randomUUID()), qty);
    }

    @Test
    void lifecycle_happyPath() {
        InventoryReservation r = InventoryReservation.pending(OrderId.newId(), List.of(item(1), item(2)), Instant.now().plusSeconds(600));
        assertEquals(InventoryReservationStatus.PENDING, r.getStatus());
        r.markReserved();
        assertEquals(InventoryReservationStatus.RESERVED, r.getStatus());
        r.confirm();
        assertEquals(InventoryReservationStatus.CONFIRMED, r.getStatus());
    }

    @Test
    void release_allowedFromPendingOrReserved() {
        InventoryReservation r1 = InventoryReservation.pending(OrderId.newId(), List.of(item(1)), Instant.now().plusSeconds(10));
        r1.release();
        assertEquals(InventoryReservationStatus.RELEASED, r1.getStatus());

        InventoryReservation r2 = InventoryReservation.pending(OrderId.newId(), List.of(item(1)), Instant.now().plusSeconds(10));
        r2.markReserved();
        r2.release();
        assertEquals(InventoryReservationStatus.RELEASED, r2.getStatus());
    }

    @Test
    void invalidTransitions_throw() {
        InventoryReservation r = InventoryReservation.pending(OrderId.newId(), List.of(item(1)), Instant.now().plusSeconds(10));
        assertThrows(IllegalStateException.class, r::confirm); // cannot confirm from pending
        r.markReserved();
        r.confirm();
        assertThrows(IllegalStateException.class, r::release); // cannot release from confirmed
        assertThrows(IllegalStateException.class, r::expire); // cannot expire from confirmed
    }
}

