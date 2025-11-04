package ie.universityofgalway.groupnine.domain.inventory;

import ie.universityofgalway.groupnine.domain.product.VariantId;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ReservationItemTest {
    @Test
    void ctor_requiresPositiveQty_andVariant() {
        VariantId vid = new VariantId(UUID.randomUUID());
        ReservationItem ok = new ReservationItem(vid, 2);
        assertEquals(2, ok.getQuantity());
        assertEquals(vid, ok.getVariantId());

        assertThrows(IllegalArgumentException.class, () -> new ReservationItem(vid, 0));
        assertThrows(IllegalArgumentException.class, () -> new ReservationItem(vid, -1));
        assertThrows(NullPointerException.class, () -> new ReservationItem(null, 1));
    }
}

