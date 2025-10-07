package ie.universityofgalway.groupnine.domain.product;

/**
 * Simple stock counters for a variant.
 *
 * @param onHand   total units physically available in inventory (>= 0)
 * @param reserved units temporarily held for orders/allocations (>= 0)
 */
public record Stock(int quantity, int reserved) {
    /**
     * Units that can still be sold (on hand minus reserved).
     *
     * @return non-negative number of sellable units
     */
    public int available() {
        return quantity - reserved;
    }
}
