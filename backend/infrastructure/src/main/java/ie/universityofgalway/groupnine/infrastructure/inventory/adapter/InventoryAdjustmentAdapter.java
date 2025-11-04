package ie.universityofgalway.groupnine.infrastructure.inventory.adapter;

import ie.universityofgalway.groupnine.domain.product.VariantId;
import ie.universityofgalway.groupnine.infrastructure.inventory.jpa.InventoryAggregateEntity;
import ie.universityofgalway.groupnine.infrastructure.inventory.jpa.InventoryAggregateJpaRepository;
import ie.universityofgalway.groupnine.infrastructure.product.jpa.VariantEntity;
import ie.universityofgalway.groupnine.infrastructure.product.jpa.VariantJpaRepository;
import ie.universityofgalway.groupnine.service.inventory.port.InventoryAdjustmentPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Infrastructure adapter that implements stock adjustments and reservations against
 * the underlying persistence stores (product variants table and inventory aggregate).
 * All methods are transactional to ensure consistency when invoked by listeners.
 */
@Component
public class InventoryAdjustmentAdapter implements InventoryAdjustmentPort {
    private final InventoryAggregateJpaRepository inventory;
    private final VariantJpaRepository variants;

    public InventoryAdjustmentAdapter(InventoryAggregateJpaRepository inventory, VariantJpaRepository variants) {
        this.inventory = inventory;
        this.variants = variants;
    }

    /** Increments the reserved counter for a variant. */
    @Override
    @Transactional
    public void incrementReserved(VariantId variantId, int quantity) {
        UUID vid = variantId.getId();
        inventory.ensureRow(vid);
        inventory.adjustReserved(vid, quantity);
    }

    /** Decrements the reserved counter for a variant. */
    @Override
    @Transactional
    public void decrementReserved(VariantId variantId, int quantity) {
        UUID vid = variantId.getId();
        inventory.adjustReserved(vid, -quantity);
    }

    /** Decrements total on‑hand stock for a variant (post‑payment confirmation). */
    @Override
    @Transactional
    public void decrementTotalStock(VariantId variantId, int quantity) {
        variants.findByUuid(variantId.getId()).ifPresent(ve -> {
            int newStock = Math.max(0, ve.getStockQuantity() - quantity);
            ve.setStockQuantity(newStock);
            ve.setAvailable(newStock > 0);
            variants.save(ve);
        });
    }

    /**
     * Attempts to reserve units atomically if sufficient supply exists.
     *
     * @return true when the reservation was applied
     */
    @Override
    @Transactional
    public boolean tryReserve(VariantId variantId, int quantity) {
        UUID vid = variantId.getId();
        inventory.ensureRow(vid);
        int updated = inventory.reserveIfAvailable(vid, quantity);
        return updated == 1;
    }
}
