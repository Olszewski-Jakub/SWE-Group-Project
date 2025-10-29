package ie.universityofgalway.groupnine.domain.product;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link Product} domain model.
 */
class ProductTest {

    /**
     * Verifies that the constructor correctly initializes all fields and that
     * the getter methods return the expected values.
     */
    @Test
    void constructor_and_getters_work_correctly() {
        ProductId id = new ProductId(UUID.randomUUID());
        Instant now = Instant.now();
        Variant variant = new Variant(new VariantId(UUID.randomUUID()), new Sku("SKU1"), null, null, List.of());

        Product product = new Product(
                id,
                "Coffee",
                "A dark roast",
                "BEVERAGE",
                ProductStatus.ACTIVE,
                List.of(variant),
                now,
                now
        );

        assertEquals(id, product.getId());
        assertEquals("Coffee", product.getName());
        assertEquals("A dark roast", product.getDescription());
        assertEquals("BEVERAGE", product.getCategory());
        assertEquals(ProductStatus.ACTIVE, product.getStatus());
        assertEquals(1, product.getVariants().size());
        assertEquals(variant, product.getVariants().get(0));
        assertEquals(now, product.getCreatedAt());
        assertEquals(now, product.getUpdatedAt());
    }

    /**
     * Tests the implementation of {@code equals()}, {@code hashCode()}, and
     * {@code toString()} to ensure they behave according to their contracts.
     */
    @Test
    void equals_and_hashCode_and_toString_behave_correctly() {
        ProductId id1 = new ProductId(UUID.randomUUID());
        Instant now = Instant.now();

        Product product1 = new Product(id1, "A", "B", "C", ProductStatus.ACTIVE, List.of(), now, now);
        Product product2 = new Product(id1, "A", "B", "C", ProductStatus.ACTIVE, List.of(), now, now);
        Product product3 = new Product(new ProductId(UUID.randomUUID()), "D", "E", "F", ProductStatus.DRAFT, List.of(), now, now);

        assertEquals(product1, product2);
        assertNotEquals(product1, product3);
        assertEquals(product1.hashCode(), product2.hashCode());
        assertNotEquals(product1.hashCode(), product3.hashCode());

        assertTrue(product1.toString().contains(id1.toString()));
        assertTrue(product1.toString().contains("name='A'"));
    }
}