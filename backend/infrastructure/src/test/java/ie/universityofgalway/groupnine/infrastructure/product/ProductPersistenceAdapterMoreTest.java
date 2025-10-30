package ie.universityofgalway.groupnine.infrastructure.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ie.universityofgalway.groupnine.domain.product.*;
import ie.universityofgalway.groupnine.infrastructure.product.adapter.ProductPersistenceAdapter;
import ie.universityofgalway.groupnine.infrastructure.product.jpa.ProductEntity;
import ie.universityofgalway.groupnine.infrastructure.product.jpa.ProductJpaRepository;
import ie.universityofgalway.groupnine.infrastructure.product.jpa.VariantEntity;
import ie.universityofgalway.groupnine.infrastructure.product.jpa.VariantJpaRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ProductPersistenceAdapterMoreTest {

    @Test
    void findAvailable_and_byCategory_delegate_and_map() {
        ProductJpaRepository repo = mock(ProductJpaRepository.class);
        VariantJpaRepository vrepo = mock(VariantJpaRepository.class);
        ProductPersistenceAdapter adapter = new ProductPersistenceAdapter(repo, vrepo);

        VariantEntity ve = new VariantEntity();
        setField(ve, "uuid", UUID.randomUUID());
        ve.setSku("SKU");
        ve.setPriceCents(250);
        ve.setCurrency("EUR");
        ve.setStockQuantity(2); ve.setReservedQuantity(0);
        // mark available indirectly by default (isAvailable true if set in entity; here defaults true)

        ProductEntity pe = new ProductEntity();
        setField(pe, "uuid", UUID.randomUUID());
        pe.setName("P"); pe.setDescription("d"); pe.setCategory("c");
        pe.setVariants(new java.util.ArrayList<>(List.of(ve)));

        when(repo.findByAvailableTrue(any())).thenReturn(new PageImpl<>(List.of(pe)));
        when(repo.findByCategoryIgnoreCaseAndAvailableTrue(eq("coffee"), any())).thenReturn(new PageImpl<>(List.of(pe)));

        Page<Product> p1 = adapter.findAvailable(PageRequest.of(0, 5));
        assertEquals(1, p1.getContent().size());
        assertEquals(ProductStatus.ACTIVE, p1.getContent().get(0).getStatus());

        Page<Product> p2 = adapter.findAvailableByCategory("coffee", PageRequest.of(1, 2));
        assertEquals(1, p2.getContent().size());
    }

    @Test
    void deleteProduct_deletes_by_internal_id_when_found() {
        ProductJpaRepository repo = mock(ProductJpaRepository.class);
        VariantJpaRepository vrepo = mock(VariantJpaRepository.class);
        ProductPersistenceAdapter adapter = new ProductPersistenceAdapter(repo, vrepo);

        UUID uuid = UUID.randomUUID();
        ProductEntity pe = new ProductEntity();
        setField(pe, "uuid", uuid);
        setField(pe, "id", 123L);
        when(repo.findByUuid(uuid)).thenReturn(Optional.of(pe));

        adapter.deleteProduct(new ProductId(uuid));
        verify(repo).deleteById(123L);
    }

    @Test
    void variantExists_and_findVariantById_delegate_and_map() {
        ProductJpaRepository repo = mock(ProductJpaRepository.class);
        VariantJpaRepository vrepo = mock(VariantJpaRepository.class);
        ProductPersistenceAdapter adapter = new ProductPersistenceAdapter(repo, vrepo);

        when(vrepo.existsBySku("X")).thenReturn(true);
        assertTrue(adapter.variantExistsBySku("X"));

        VariantEntity ve = new VariantEntity();
        UUID vid = UUID.randomUUID();
        setField(ve, "uuid", vid);
        ve.setSku("SKU1");
        ve.setPriceCents(123);
        ve.setCurrency(null); // default to EUR path
        ve.setStockQuantity(1); ve.setReservedQuantity(0);
        ObjectMapper om = new ObjectMapper();
        ObjectNode attrs = om.createObjectNode();
        attrs.put("color", "red");
        ve.setAttributes(attrs);
        when(vrepo.findByUuid(vid)).thenReturn(Optional.of(ve));

        Variant v = adapter.findVariantById(new VariantId(vid)).orElseThrow();
        assertEquals("SKU1", v.getSku().getValue());
        assertEquals(new BigDecimal("1.23"), v.getPrice().getAmount());
        assertEquals(Currency.getInstance("EUR"), v.getPrice().getCurrency());
        assertEquals(1, v.getAttributes().size());
        assertEquals("color", v.getAttributes().get(0).name());
        assertEquals("red", v.getAttributes().get(0).value());
    }

    @Test
    void saveVariant_inserts_when_missing_and_rounds_amount_half_up() {
        ProductJpaRepository repo = mock(ProductJpaRepository.class);
        VariantJpaRepository vrepo = mock(VariantJpaRepository.class);
        ProductPersistenceAdapter adapter = new ProductPersistenceAdapter(repo, vrepo);

        UUID pid = UUID.randomUUID();
        ProductEntity pe = new ProductEntity();
        setField(pe, "uuid", pid);
        when(repo.findByUuid(pid)).thenReturn(Optional.of(pe));
        when(vrepo.findByUuid(any())).thenReturn(Optional.empty());

        ArgumentCaptor<VariantEntity> savedCap = ArgumentCaptor.forClass(VariantEntity.class);
        when(vrepo.save(savedCap.capture())).thenAnswer(inv -> inv.getArgument(0));

        Variant v = new Variant(new VariantId(UUID.randomUUID()), new Sku("S"), new Money(new BigDecimal("9.995"), Currency.getInstance("EUR")), new Stock(0,0), List.of());
        Variant saved = adapter.saveVariant(new ProductId(pid), v);

        assertEquals(1000, savedCap.getValue().getPriceCents());
        assertEquals(new BigDecimal("10"), saved.getPrice().getAmount());
    }

    private static void setField(Object target, String field, Object value) {
        try {
            var f = target.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

