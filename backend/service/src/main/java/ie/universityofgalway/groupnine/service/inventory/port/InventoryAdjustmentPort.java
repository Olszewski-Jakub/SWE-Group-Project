package ie.universityofgalway.groupnine.service.inventory.port;

import ie.universityofgalway.groupnine.domain.product.VariantId;

/**
 * Port to adjust decoupled inventory counters and total stock.
 * Implementations are responsible for atomicity and persistence concerns.
 */
public interface InventoryAdjustmentPort {
    void incrementReserved(VariantId variantId, int quantity);
    void decrementReserved(VariantId variantId, int quantity);
    void decrementTotalStock(VariantId variantId, int quantity);
    /**
     * Attempts to reserve quantity atomically only if available (total - reserved >= quantity).
     * Returns true if reserved, false otherwise (no side effects if false).
     */
    boolean tryReserve(VariantId variantId, int quantity);
}
