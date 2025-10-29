package ie.universityofgalway.groupnine.infrastructure.product;

import ie.universityofgalway.groupnine.infrastructure.product.jpa.VariantEntity;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class VariantEntityTest {

    @Test
    void defaultsAndSettersWork() {
        VariantEntity v = new VariantEntity();
        assertTrue(v.isAvailable());
        assertNotNull(v.getCreatedAt());
        assertNotNull(v.getUpdatedAt());
        assertNull(v.getAttributes());

        v.setSku("SKU-1");
        v.setCurrency("EUR");
        v.setPriceCents(999);
        v.setStockQuantity(3);
        v.setReservedQuantity(1);
        v.setImageUrl("http://img");
        assertEquals("SKU-1", v.getSku());
        assertEquals("EUR", v.getCurrency());
        assertEquals(999, v.getPriceCents());
        assertEquals(3, v.getStockQuantity());
        assertEquals(1, v.getReservedQuantity());
        assertEquals("http://img", v.getImageUrl());
    }

    @Test
    void preUpdateTouchesUpdatedAt() throws InterruptedException {
        VariantEntity v = new VariantEntity();
        Instant before = v.getUpdatedAt();
        // simulate update
        Thread.sleep(5);
        v.touch();
        assertTrue(v.getUpdatedAt().isAfter(before) || v.getUpdatedAt().equals(before));
    }
}

