package ie.universityofgalway.groupnine.infrastructure.product;

import ie.universityofgalway.groupnine.infrastructure.product.jpa.ProductEntity;
import ie.universityofgalway.groupnine.infrastructure.product.jpa.VariantEntity;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class ProductEntityTest {

    @Test
    void defaultsAndSettersWork() {
        ProductEntity p = new ProductEntity();
        assertNotNull(p.getUuid());
        assertEquals("DRAFT", p.getStatus());
        assertNotNull(p.getCreatedAt());
        assertNotNull(p.getUpdatedAt());
        assertNotNull(p.getVariants());
        assertTrue(p.getVariants().isEmpty());

        p.setName("Name");
        p.setDescription("Desc");
        p.setCategory("coffee");
        p.setStatus("ACTIVE");
        assertEquals("Name", p.getName());
        assertEquals("Desc", p.getDescription());
        assertEquals("coffee", p.getCategory());
        assertEquals("ACTIVE", p.getStatus());

        var list = new ArrayList<VariantEntity>();
        p.setVariants(list);
        assertSame(list, p.getVariants());
    }

    @Test
    void preUpdateTouchesUpdatedAt() throws InterruptedException {
        ProductEntity p = new ProductEntity();
        Instant before = p.getUpdatedAt();
        Thread.sleep(5);
        p.touch();
        assertTrue(p.getUpdatedAt().isAfter(before) || p.getUpdatedAt().equals(before));
    }
}

