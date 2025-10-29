package ie.universityofgalway.groupnine.service.product.admin.usecase;

import ie.universityofgalway.groupnine.domain.product.*;
import ie.universityofgalway.groupnine.service.product.port.ProductPort;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CreateProductUseCaseTest {

    private static class StubPort implements ProductPort {
        @Override public Page<Product> findAvailable(Pageable pageable) { return Page.empty(); }
        @Override public Page<Product> findAvailableByCategory(String category, Pageable pageable) { return Page.empty(); }
        @Override public Page<Product> search(SearchQuery query, Pageable pageable) { return Page.empty(); }
        @Override public Optional<Product> findById(ProductId id) { return Optional.empty(); }
        @Override public Page<Product> listAll(Pageable pageable) { return Page.empty(); }
        @Override public boolean productExistsByUuid(UUID uuid) { return false; }
        @Override public Product saveProduct(Product product) { return product; }
        @Override public void deleteProduct(ProductId id) { }
        @Override public boolean variantExistsBySku(String sku) { return false; }
        @Override public Optional<Variant> findVariantById(VariantId id) { return Optional.empty(); }
        @Override public Variant saveVariant(ProductId productId, Variant variant) { return variant; }
        @Override public void deleteVariant(ProductId productId, VariantId variantId) { }
    }

    @Test
    void createGeneratesIdsWhenMissing() {
        ProductPort port = new StubPort();
        CreateProductUseCase uc = new CreateProductUseCase(port);

        Variant v = new Variant(null, new Sku("SKU-1"), new Money(new BigDecimal("9.99"), Currency.getInstance("EUR")), new Stock(10, 0), java.util.List.of());
        Product input = new Product(
                null,
                "Test Product",
                "desc",
                "coffee",
                ProductStatus.DRAFT,
                java.util.List.of(v),
                null,
                null
        );

        Product out = uc.execute(input);
        assertNotNull(out.getId());
        assertNotNull(out.getId().getId());
        assertEquals(1, out.getVariants().size());
        assertNotNull(out.getVariants().get(0).getId());
        assertNotNull(out.getVariants().get(0).getId().getId());
    }

    @Test
    void createValidatesStockInvariant() {
        ProductPort port = new StubPort();
        CreateProductUseCase uc = new CreateProductUseCase(port);

        Variant v = new Variant(null, new Sku("SKU-2"), new Money(new BigDecimal("3.00"), Currency.getInstance("EUR")), new Stock(1, 2), java.util.List.of());
        Product input = new Product(null, "P", "d", "coffee", ProductStatus.DRAFT, java.util.List.of(v), null, null);

        assertThrows(IllegalArgumentException.class, () -> uc.execute(input));
    }
}
