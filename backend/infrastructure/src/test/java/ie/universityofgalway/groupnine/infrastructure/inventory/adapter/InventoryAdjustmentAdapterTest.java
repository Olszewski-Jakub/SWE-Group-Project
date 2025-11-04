package ie.universityofgalway.groupnine.infrastructure.inventory.adapter;

import ie.universityofgalway.groupnine.domain.product.VariantId;
import ie.universityofgalway.groupnine.infrastructure.inventory.jpa.InventoryAggregateJpaRepository;
import ie.universityofgalway.groupnine.infrastructure.product.jpa.VariantEntity;
import ie.universityofgalway.groupnine.infrastructure.product.jpa.VariantJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class InventoryAdjustmentAdapterTest {

    private InventoryAggregateJpaRepository inventory;
    private VariantJpaRepository variants;
    private InventoryAdjustmentAdapter adapter;

    @BeforeEach
    void setup() {
        inventory = Mockito.mock(InventoryAggregateJpaRepository.class);
        variants = Mockito.mock(VariantJpaRepository.class);
        adapter = new InventoryAdjustmentAdapter(inventory, variants);
    }

    @Test
    void incrementReserved_callsEnsureAndAdjust() {
        UUID vid = UUID.randomUUID();
        adapter.incrementReserved(new VariantId(vid), 3);
        verify(inventory).ensureRow(eq(vid));
        verify(inventory).adjustReserved(eq(vid), eq(3));
    }

    @Test
    void decrementReserved_adjustsNegativeDelta() {
        UUID vid = UUID.randomUUID();
        adapter.decrementReserved(new VariantId(vid), 2);
        verify(inventory).adjustReserved(eq(vid), eq(-2));
    }

    @Test
    void tryReserve_delegates_to_repo_and_returns_boolean() {
        UUID vid = UUID.randomUUID();
        when(inventory.reserveIfAvailable(eq(vid), eq(5))).thenReturn(1);
        assertTrue(adapter.tryReserve(new VariantId(vid), 5));
        when(inventory.reserveIfAvailable(eq(vid), eq(5))).thenReturn(0);
        assertFalse(adapter.tryReserve(new VariantId(vid), 5));
        verify(inventory, atLeastOnce()).ensureRow(eq(vid));
    }

    @Test
    void decrementTotalStock_updates_variant_quantity_and_availability() {
        UUID vid = UUID.randomUUID();
        VariantEntity ve = new VariantEntity();
        ve.setStockQuantity(4);
        ve.setAvailable(true);
        when(variants.findByUuid(eq(vid))).thenReturn(Optional.of(ve));

        adapter.decrementTotalStock(new VariantId(vid), 3);

        ArgumentCaptor<VariantEntity> cap = ArgumentCaptor.forClass(VariantEntity.class);
        verify(variants).save(cap.capture());
        assertEquals(1, cap.getValue().getStockQuantity());
        assertTrue(cap.getValue().isAvailable());

        // Decrement beyond zero clamps to 0 and available=false
        ve.setStockQuantity(1);
        ve.setAvailable(true);
        adapter.decrementTotalStock(new VariantId(vid), 5);
        verify(variants, times(2)).save(cap.capture());
        assertEquals(0, cap.getValue().getStockQuantity());
        assertFalse(cap.getValue().isAvailable());
    }
}

