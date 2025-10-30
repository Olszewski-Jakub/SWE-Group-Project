package ie.universityofgalway.groupnine.domain.inventory;

import ie.universityofgalway.groupnine.domain.product.VariantId;

import java.util.Objects;

/**
 * Minimal representation of an item reserved for an order: variant id and quantity.
 */
public final class ReservationItem {
    private final VariantId variantId;
    private final int quantity;

    public ReservationItem(VariantId variantId, int quantity) {
        this.variantId = Objects.requireNonNull(variantId, "variantId");
        if (quantity <= 0) throw new IllegalArgumentException("quantity must be > 0");
        this.quantity = quantity;
    }

    public VariantId getVariantId() { return variantId; }
    public int getQuantity() { return quantity; }
}

