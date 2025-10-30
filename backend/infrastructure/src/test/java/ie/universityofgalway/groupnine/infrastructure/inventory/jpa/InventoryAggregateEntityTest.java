package ie.universityofgalway.groupnine.infrastructure.inventory.jpa;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InventoryAggregateEntityTest {

    @Test
    void gettersSetters_and_touch_updateTimestamp() {
        InventoryAggregateEntity e = new InventoryAggregateEntity();

        UUID vid = UUID.randomUUID();
        e.setVariantId(vid);
        e.setReserved(5);

        assertEquals(vid, e.getVariantId());
        assertEquals(5, e.getReserved());
        assertNotNull(e.getUpdatedAt());

        Instant before = e.getUpdatedAt();
        // invoke lifecycle method explicitly
        e.touch();
        Instant after = e.getUpdatedAt();

        assertNotNull(after);
        // touch should not move time backwards
        assertFalse(after.isBefore(before));
    }
}

