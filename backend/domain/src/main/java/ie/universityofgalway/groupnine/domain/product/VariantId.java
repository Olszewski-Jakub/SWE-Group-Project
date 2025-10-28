package ie.universityofgalway.groupnine.domain.product;

import java.util.UUID;
import java.util.Objects;

/**
 * Value object representing the public identifier of a {@link Variant}.
 * Converted from a record to a class.
 */
public final class VariantId {

    private final UUID id;

    public VariantId(UUID id) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
    }

    // Getter
    public UUID getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VariantId variantId = (VariantId) o;
        return id.equals(variantId.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "VariantId[id=" + id + ']';
    }
}
