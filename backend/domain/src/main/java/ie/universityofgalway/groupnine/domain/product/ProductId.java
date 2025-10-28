package ie.universityofgalway.groupnine.domain.product;

import java.util.UUID;
import java.util.Objects;

/**
 * Value object representing the public identifier of a {@link Product}.
 * Converted from a record to a class.
 */
public final class ProductId {

    private final UUID id;

    public ProductId(UUID id) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
    }

    /**
     * Convenience constructor to parse a UUID from its string form.
     */
    public ProductId(String id) {
        this(UUID.fromString(id));
    }

    // Getter
    public UUID getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductId productId = (ProductId) o;
        return id.equals(productId.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ProductId[id=" + id + ']';
    }
}
