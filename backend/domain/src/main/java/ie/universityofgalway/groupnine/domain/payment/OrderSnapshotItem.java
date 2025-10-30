package ie.universityofgalway.groupnine.domain.payment;

import ie.universityofgalway.groupnine.domain.product.VariantId;

import java.util.Currency;
import java.util.Objects;

public final class OrderSnapshotItem {
    private final VariantId variantId;
    private final String sku;
    private final long unitAmountMinor;
    private final int quantity;
    private final Currency currency;

    public OrderSnapshotItem(VariantId variantId, String sku, long unitAmountMinor, int quantity, Currency currency) {
        this.variantId = Objects.requireNonNull(variantId);
        this.sku = Objects.requireNonNull(sku);
        this.unitAmountMinor = unitAmountMinor;
        if (quantity <= 0) throw new IllegalArgumentException("quantity must be > 0");
        this.quantity = quantity;
        this.currency = Objects.requireNonNull(currency);
    }

    public VariantId getVariantId() { return variantId; }
    public String getSku() { return sku; }
    public long getUnitAmountMinor() { return unitAmountMinor; }
    public int getQuantity() { return quantity; }
    public Currency getCurrency() { return currency; }
}

