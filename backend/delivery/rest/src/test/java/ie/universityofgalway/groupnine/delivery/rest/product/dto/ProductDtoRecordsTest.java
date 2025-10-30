package ie.universityofgalway.groupnine.delivery.rest.product.dto;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductDtoRecordsTest {

    @Test
    void variantResponse_holdsValues() {
        VariantResponse v = new VariantResponse("SKU-123", 1599, "EUR");
        assertEquals("SKU-123", v.sku());
        assertEquals(1599, v.priceCents());
        assertEquals("EUR", v.currency());
    }

    @Test
    void productResponse_holdsValues() {
        Instant created = Instant.parse("2024-01-01T00:00:00Z");
        Instant updated = Instant.parse("2024-01-02T00:00:00Z");
        ProductResponse p = new ProductResponse(
                "id-1",
                "Coffee",
                "Rich taste",
                "beverages",
                "ACTIVE",
                List.of(new VariantResponse("SKU-1", 250, "EUR")),
                created,
                updated
        );

        assertEquals("id-1", p.id());
        assertEquals("Coffee", p.name());
        assertEquals("Rich taste", p.description());
        assertEquals("beverages", p.category());
        assertEquals("ACTIVE", p.status());
        assertEquals(1, p.variants().size());
        assertEquals(created, p.createdAt());
        assertEquals(updated, p.updatedAt());
    }

    @Test
    void pageResponse_holdsValues() {
        List<String> content = List.of("a", "b");
        PageResponse<String> page = new PageResponse<>(content, 2, 10, 42, 5);

        assertEquals(content, page.content());
        assertEquals(2, page.page());
        assertEquals(10, page.size());
        assertEquals(42, page.totalElements());
        assertEquals(5, page.totalPages());
    }
}

