package ie.universityofgalway.groupnine.infrastructure.product;

import ie.universityofgalway.groupnine.domain.product.*;
import ie.universityofgalway.groupnine.infrastructure.product.adapter.ProductPersistenceAdapter;
import ie.universityofgalway.groupnine.infrastructure.product.jpa.ProductEntity;
import ie.universityofgalway.groupnine.infrastructure.product.jpa.ProductJpaRepository;
import ie.universityofgalway.groupnine.infrastructure.product.jpa.VariantEntity;
import ie.universityofgalway.groupnine.infrastructure.product.jpa.VariantJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductPersistenceAdapterWriteTest {

    @Mock
    ProductJpaRepository productRepo;
    @Mock
    VariantJpaRepository variantRepo;

    @InjectMocks
    ProductPersistenceAdapter adapter;

    @Test
    void saveProduct_mergesExistingVariant_andUpdatesFields() {
        UUID pid = UUID.randomUUID();
        UUID vid = UUID.randomUUID();
        ProductEntity entity = new ProductEntity();
        set(entity, "uuid", pid);

        VariantEntity existingVe = new VariantEntity();
        set(existingVe, "uuid", vid);
        existingVe.setSku("OLD-SKU");
        existingVe.setPriceCents(100);
        existingVe.setCurrency("EUR");
        entity.setVariants(new java.util.ArrayList<>(List.of(existingVe)));

        when(productRepo.findByUuid(pid)).thenReturn(Optional.of(entity));
        when(productRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Product domain = new Product(new ProductId(pid), "P", "d", "c", ProductStatus.DRAFT,
                List.of(new Variant(new VariantId(vid), new Sku("NEW-SKU"), new Money(new BigDecimal("9.99"), Currency.getInstance("USD")), new Stock(5, 1), List.of(), "/products/p/variants/v/image")),
                null, null);

        Product out = adapter.saveProduct(domain);

        ArgumentCaptor<ProductEntity> savedCap = ArgumentCaptor.forClass(ProductEntity.class);
        verify(productRepo).save(savedCap.capture());
        ProductEntity saved = savedCap.getValue();
        assertEquals(1, saved.getVariants().size());
        VariantEntity ve = saved.getVariants().get(0);
        assertEquals(vid, ve.getUuid());
        assertEquals("NEW-SKU", ve.getSku());
        assertEquals(999, ve.getPriceCents());
        assertEquals("USD", ve.getCurrency());
        assertEquals(5, ve.getStockQuantity());
        assertEquals(1, ve.getReservedQuantity());
        assertEquals(pid, out.getId().getId());
        assertEquals("/products/p/variants/v/image", ve.getImageUrl());
    }

    @Test
    void saveProduct_removesMissingVariant() {
        UUID pid = UUID.randomUUID();
        UUID vid = UUID.randomUUID();
        ProductEntity entity = new ProductEntity();
        set(entity, "uuid", pid);

        VariantEntity existingVe = new VariantEntity();
        set(existingVe, "uuid", vid);
        entity.setVariants(new java.util.ArrayList<>(List.of(existingVe)));

        when(productRepo.findByUuid(pid)).thenReturn(Optional.of(entity));
        when(productRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Product domain = new Product(new ProductId(pid), "P", "d", "c", ProductStatus.DRAFT, List.of(), null, null);
        adapter.saveProduct(domain);

        ArgumentCaptor<ProductEntity> savedCap = ArgumentCaptor.forClass(ProductEntity.class);
        verify(productRepo).save(savedCap.capture());
        assertTrue(savedCap.getValue().getVariants().isEmpty());
    }

    @Test
    void deleteVariant_deletesFoundEntity() {
        UUID vid = UUID.randomUUID();
        VariantEntity ve = new VariantEntity();
        set(ve, "uuid", vid);
        when(variantRepo.findByUuid(vid)).thenReturn(Optional.of(ve));
        adapter.deleteVariant(new ProductId(UUID.randomUUID()), new VariantId(vid));
        verify(variantRepo).delete(ve);
    }

    @Test
    void saveVariant_setsImageUrlOnEntity() {
        UUID pid = UUID.randomUUID();
        UUID vid = UUID.randomUUID();
        ProductEntity product = new ProductEntity();
        set(product, "uuid", pid);
        when(productRepo.findByUuid(pid)).thenReturn(Optional.of(product));

        when(variantRepo.findByUuid(vid)).thenReturn(Optional.empty());
        when(variantRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Variant domainVariant = new Variant(new VariantId(vid), new Sku("S"), new Money(new BigDecimal("1.00"), Currency.getInstance("EUR")), new Stock(1,0), List.of(), "/products/p/variants/v/image");
        Variant saved = adapter.saveVariant(new ProductId(pid), domainVariant);

        ArgumentCaptor<VariantEntity> cap = ArgumentCaptor.forClass(VariantEntity.class);
        verify(variantRepo).save(cap.capture());
        VariantEntity persisted = cap.getValue();
        assertEquals("/products/p/variants/v/image", persisted.getImageUrl());
    }

    private static void set(Object target, String field, Object value) {
        try {
            var f = target.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
