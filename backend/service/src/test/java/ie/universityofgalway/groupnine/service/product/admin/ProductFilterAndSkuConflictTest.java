package ie.universityofgalway.groupnine.service.product.admin;

import ie.universityofgalway.groupnine.domain.product.ProductStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProductFilterAndSkuConflictTest {

    @Test
    void productFilter_getters_setters_and_ctor() {
        ProductFilter f1 = new ProductFilter();
        f1.setCategory("coffee");
        f1.setStatus(ProductStatus.ACTIVE);
        assertEquals("coffee", f1.getCategory());
        assertEquals(ProductStatus.ACTIVE, f1.getStatus());

        ProductFilter f2 = new ProductFilter("espresso", ProductStatus.DRAFT);
        assertEquals("espresso", f2.getCategory());
        assertEquals(ProductStatus.DRAFT, f2.getStatus());
    }

    @Test
    void skuConflictException_message_contains_sku() {
        SkuConflictException ex = new SkuConflictException("SKU-1");
        assertTrue(ex.getMessage().contains("SKU-1"));
        assertTrue(ex.getMessage().startsWith("SKU already exists:"));
    }
}

