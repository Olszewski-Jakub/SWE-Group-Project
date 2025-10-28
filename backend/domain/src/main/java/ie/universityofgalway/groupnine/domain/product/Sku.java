package ie.universityofgalway.groupnine.domain.product;

import java.util.Objects;

/**
 * Stock Keeping Unit (SKU) code.
 * Converted from a record to a class.
 */
public final class Sku {

    private final String value;

    public Sku(String value) {
        this.value = Objects.requireNonNull(value, "value cannot be null");
    }

    // Getter
    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sku sku = (Sku) o;
        return value.equals(sku.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "Sku[value='" + value + '\'' + ']';
    }
}
