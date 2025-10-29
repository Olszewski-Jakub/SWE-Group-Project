package ie.universityofgalway.groupnine.infrastructure.product;

import ie.universityofgalway.groupnine.domain.product.*;
import ie.universityofgalway.groupnine.infrastructure.product.adapter.VariantPersistenceAdapter;
import ie.universityofgalway.groupnine.infrastructure.product.jpa.VariantEntity;
import ie.universityofgalway.groupnine.infrastructure.product.jpa.VariantJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VariantPersistenceAdapterTest {

    @Mock
    private VariantJpaRepository repo;

    @InjectMocks
    private VariantPersistenceAdapter adapter;

    @Test
    void mapsEntityToDomain_withExplicitCurrency() {
        UUID vid = UUID.randomUUID();
        VariantEntity e = new VariantEntity();
        // set uuid via reflection since field is private with default random
        set(e, "uuid", vid);
        e.setSku("SKU-123");
        e.setPriceCents(1234);
        e.setCurrency("USD");
        e.setStockQuantity(7);
        e.setReservedQuantity(2);

        when(repo.findByUuid(vid)).thenReturn(Optional.of(e));

        Optional<Variant> rs = adapter.findById(new VariantId(vid));
        assertTrue(rs.isPresent());
        Variant v = rs.get();
        assertEquals("SKU-123", v.getSku().getValue());
        assertEquals(0, new BigDecimal("12.34").compareTo(v.getPrice().getAmount()));
        assertEquals(Currency.getInstance("USD"), v.getPrice().getCurrency());
        assertEquals(7, v.getStock().getQuantity());
        assertEquals(2, v.getStock().getReserved());
    }

    @Test
    void defaultsCurrencyToEur_whenNull() {
        UUID vid = UUID.randomUUID();
        VariantEntity e = new VariantEntity();
        set(e, "uuid", vid);
        e.setSku("SKU-123");
        e.setPriceCents(100);
        e.setCurrency(null);
        e.setStockQuantity(1);
        e.setReservedQuantity(0);

        when(repo.findByUuid(vid)).thenReturn(Optional.of(e));
        Variant v = adapter.findById(new VariantId(vid)).orElseThrow();
        assertEquals(Currency.getInstance("EUR"), v.getPrice().getCurrency());
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

