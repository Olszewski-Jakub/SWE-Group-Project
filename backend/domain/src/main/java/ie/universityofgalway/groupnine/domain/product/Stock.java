package ie.universityofgalway.groupnine.domain.product;

import java.util.Objects;

/**
 * Simple stock counters for a variant.
 * Converted from a record to a class.
 */
public final class Stock {

    private final int quantity;
    private final int reserved;

    public Stock(int quantity, int reserved) {
        this.quantity = quantity;
        this.reserved = reserved;
    }

    // Getters
    public int getQuantity() { return quantity; }
    public int getReserved() { return reserved; }

    /**
     * Units that can still be sold (on hand minus reserved).
     */
    public int available() {
        return quantity - reserved;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Stock stock = (Stock) o;
        return quantity == stock.quantity && reserved == stock.reserved;
    }

    @Override
    public int hashCode() {
        return Objects.hash(quantity, reserved);
    }

    @Override
    public String toString() {
        return "Stock[quantity=" + quantity + ", reserved=" + reserved + ']';
    }
}
