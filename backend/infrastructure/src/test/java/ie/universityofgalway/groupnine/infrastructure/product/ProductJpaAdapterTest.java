package ie.universityofgalway.groupnine.infrastructure.product;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ie.universityofgalway.groupnine.domain.product.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link ProductJpaAdapter}.
 *
 * <p>These tests verify the mapping logic from persistence-layer entities
 * ({@link ProductEntity}, {@link VariantEntity}) to domain models
 * ({@link Product}, {@link Variant}). The repository is mocked so tests
 * focus solely on adapter behavior and mapping rules.
 *
 * <h2>Coverage</h2>
 * <ul>
 *   <li>Successful lookup maps all primary fields and variant details.</li>
 *   <li>Missing product returns an empty {@link Optional}.</li>
 *   <li>Product status is set to {@link ProductStatus#DRAFT} if no variants are available.</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class ProductJpaAdapterTest {

    @Mock
    private ProductJpaRepository productJpaRepository;

    @InjectMocks
    private ProductPersistenceAdapter productJpaAdapter;

    /**
     * Given a product and one available variant in the persistence layer,
     * when {@link ProductJpaAdapter#findById(ProductId)} is called,
     * then the domain {@link Product} is returned with correctly mapped fields.
     *
     * <p><strong>Asserts:</strong>
     * <ul>
     *   <li>Product UUID, name, and status are correctly set.</li>
     *   <li>Variant list contains exactly one element.</li>
     *   <li>Variant ID, SKU, price amount, and stock mapping are correct.</li>
     * </ul>
     */
    @Test
    void findById_whenProductExists_shouldMapEntityToDomainCorrectly() {
        UUID productUuid = UUID.randomUUID();
        UUID variantUuid = UUID.randomUUID();

        VariantEntity variantEntity = mock(VariantEntity.class);
        when(variantEntity.getUuid()).thenReturn(variantUuid);
        when(variantEntity.getSku()).thenReturn("TEST-SKU-123");
        when(variantEntity.getPriceCents()).thenReturn(9999);
        when(variantEntity.getCurrency()).thenReturn("EUR");
        when(variantEntity.getStockQuantity()).thenReturn(100);
        when(variantEntity.getReservedQuantity()).thenReturn(10);
        when(variantEntity.isAvailable()).thenReturn(true);

        ProductEntity productEntity = mock(ProductEntity.class);
        when(productEntity.getUuid()).thenReturn(productUuid);
        when(productEntity.getName()).thenReturn("Test Product");
        when(productEntity.getDescription()).thenReturn("A product for testing.");
        when(productEntity.getCategory()).thenReturn("Testing");
        when(productEntity.getVariants()).thenReturn(List.of(variantEntity));

        when(productJpaRepository.findByUuid(productUuid)).thenReturn(Optional.of(productEntity));

        Optional<Product> result = productJpaAdapter.findById(new ProductId(productUuid));

        assertTrue(result.isPresent(), "Product should be found");
        Product product = result.get();

        assertEquals(productUuid, product.id().id());
        assertEquals("Test Product", product.name());
        assertEquals(ProductStatus.ACTIVE, product.status());
        assertEquals(1, product.variants().size(), "Should be one variant");

        Variant variant = product.variants().get(0);
        assertEquals(variantUuid, variant.id().id());
        assertEquals("TEST-SKU-123", variant.sku().value());
        assertEquals(0, new BigDecimal("99.99").compareTo(variant.price().amount()));
        // NOTE: adjust this assertion if your Stock record exposes different accessors.
        assertEquals(100, variant.stock().quantity());
    }

    /**
     * Given the repository returns {@link Optional#empty()} for a UUID,
     * when {@link ProductJpaAdapter#findById(ProductId)} is called,
     * then an empty {@link Optional} is propagated to the caller.
     */
    @Test
    void findById_whenProductNotFound_shouldReturnEmptyOptional() {
        UUID nonExistentUuid = UUID.randomUUID();
        when(productJpaRepository.findByUuid(nonExistentUuid)).thenReturn(Optional.empty());
        ProductId productId = new ProductId(nonExistentUuid);

        Optional<Product> result = productJpaAdapter.findById(productId);

        assertTrue(result.isEmpty(), "Product should not be found");
    }

    /**
     * Given a product whose variants are all unavailable,
     * when {@link ProductJpaAdapter#findById(ProductId)} maps the entity,
     * then the resulting domain {@link Product} has status {@link ProductStatus#DRAFT}.
     */
    @Test
    void findById_shouldSetStatusToDraft_whenNoVariantsAreAvailable() {
        UUID productUuid = UUID.randomUUID();

        VariantEntity variantEntity = mock(VariantEntity.class);
        when(variantEntity.isAvailable()).thenReturn(false);

        ProductEntity productEntity = mock(ProductEntity.class);
        when(productEntity.getUuid()).thenReturn(productUuid);
        when(productEntity.getVariants()).thenReturn(List.of(variantEntity));

        when(productJpaRepository.findByUuid(productUuid)).thenReturn(Optional.of(productEntity));

        Optional<Product> result = productJpaAdapter.findById(new ProductId(productUuid));

        assertTrue(result.isPresent());
        assertEquals(ProductStatus.DRAFT, result.get().status(), "Status should be DRAFT");
    }
}
