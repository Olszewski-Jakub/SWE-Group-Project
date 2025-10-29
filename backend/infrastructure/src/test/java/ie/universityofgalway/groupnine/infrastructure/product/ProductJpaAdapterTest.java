package ie.universityofgalway.groupnine.infrastructure.product;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ie.universityofgalway.groupnine.domain.product.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ie.universityofgalway.groupnine.infrastructure.product.adapter.ProductPersistenceAdapter;
import ie.universityofgalway.groupnine.infrastructure.product.jpa.ProductEntity;
import ie.universityofgalway.groupnine.infrastructure.product.jpa.ProductJpaRepository;
import ie.universityofgalway.groupnine.infrastructure.product.jpa.VariantEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for the {@link ProductPersistenceAdapter}.
 */
@ExtendWith(MockitoExtension.class)
class ProductJpaAdapterTest {

    @Mock
    private ProductJpaRepository productJpaRepository;

    @InjectMocks
    private ProductPersistenceAdapter productPersistenceAdapter;

    /**
     * Verifies that when a product entity exists in the repository, it is correctly
     * mapped to a {@link Product} domain object with all its fields, variants,
     * and a status of ACTIVE.
     */
    @Test
    void findById_whenProductExists_shouldMapEntityToDomainCorrectly() {
        UUID productUuid = UUID.randomUUID();
        UUID variantUuid = UUID.randomUUID();
        Currency eur = Currency.getInstance("EUR");

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
        when(productEntity.getCreatedAt()).thenReturn(Instant.now());
        when(productEntity.getUpdatedAt()).thenReturn(Instant.now());

        when(productJpaRepository.findByUuid(productUuid)).thenReturn(Optional.of(productEntity));

        Optional<Product> result = productPersistenceAdapter.findById(new ProductId(productUuid));

        assertTrue(result.isPresent());
        Product product = result.get();
        assertEquals(productUuid, product.getId().getId());
        assertEquals(ProductStatus.ACTIVE, product.getStatus());
        
        Variant variant = product.getVariants().get(0);
        assertEquals(variantUuid, variant.getId().getId());
        assertEquals("TEST-SKU-123", variant.getSku().getValue());
        assertEquals(0, new BigDecimal("99.99").compareTo(variant.getPrice().getAmount()));
        assertEquals(eur, variant.getPrice().getCurrency());
        assertEquals(100, variant.getStock().getQuantity());
        assertEquals(10, variant.getStock().getReserved());
        assertEquals(90, variant.getStock().available());
    }
    
    /**
     * Verifies that if a product's variants are all marked as unavailable,
     * the resulting domain object's status is correctly set to DRAFT.
     */
    @Test
    void findById_shouldSetStatusToDraft_whenNoVariantsAreAvailable() {
        UUID productUuid = UUID.randomUUID();
        VariantEntity variantEntity = mock(VariantEntity.class);
        when(variantEntity.isAvailable()).thenReturn(false);
        when(variantEntity.getCurrency()).thenReturn("EUR");
        when(variantEntity.getUuid()).thenReturn(UUID.randomUUID());
        when(variantEntity.getSku()).thenReturn("SKU-DRAFT");
        when(variantEntity.getPriceCents()).thenReturn(1000);
        when(variantEntity.getStockQuantity()).thenReturn(5);
        when(variantEntity.getReservedQuantity()).thenReturn(0);

        ProductEntity productEntity = mock(ProductEntity.class);
        when(productEntity.getUuid()).thenReturn(productUuid);
        when(productEntity.getVariants()).thenReturn(List.of(variantEntity));
        when(productEntity.getName()).thenReturn("Draft Product");
        when(productEntity.getCategory()).thenReturn("Drafts");
        when(productEntity.getCreatedAt()).thenReturn(Instant.now());
        when(productEntity.getUpdatedAt()).thenReturn(Instant.now());

        when(productJpaRepository.findByUuid(productUuid)).thenReturn(Optional.of(productEntity));
        Optional<Product> result = productPersistenceAdapter.findById(new ProductId(productUuid));

        assertTrue(result.isPresent());
        assertEquals(ProductStatus.DRAFT, result.get().getStatus());
    }
}