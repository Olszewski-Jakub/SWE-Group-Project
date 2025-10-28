package ie.universityofgalway.groupnine.domain.product;

import java.util.List;
import java.util.Objects;

/**
 * Domain model for a concrete, sellable variation of a {@link Product}.
 * Converted from a record to a class.
 */
public final class Variant {

    private final VariantId id;
    private final Sku sku;
    private final Money price;
    private final Stock stock;
    private final List<Attribute> attributes;

    public Variant(VariantId id, Sku sku, Money price, Stock stock, List<Attribute> attributes) {
        this.id = id;
        this.sku = sku;
        this.price = price;
        this.stock = stock;
        this.attributes = List.copyOf(attributes); // Ensure immutability
    }

    // Getters
    public VariantId getId() { return id; }
    public Sku getSku() { return sku; }
    public Money getPrice() { return price; }
    public Stock getStock() { return stock; }
    public List<Attribute> getAttributes() { return List.copyOf(attributes); } // Return immutable view

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Variant variant = (Variant) o;
        return Objects.equals(id, variant.id) &&
               Objects.equals(sku, variant.sku) &&
               Objects.equals(price, variant.price) &&
               Objects.equals(stock, variant.stock) &&
               Objects.equals(attributes, variant.attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, sku, price, stock, attributes);
    }

    @Override
    public String toString() {
        return "Variant[" +
               "id=" + id + ", " +
               "sku=" + sku + ", " +
               "price=" + price + ", " +
               "stock=" + stock + ", " +
               "attributes=" + attributes +
               ']';
    }
}
