package ie.universityofgalway.groupnine.domain.cart;

import java.util.Objects;
import java.util.UUID;

/**
 * Value object representing the unique public identifier of a ShoppingCart.
 */
public class CartId {

    private final UUID id;

    public CartId(UUID id) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
    }

    public CartId(String id) {
        this(UUID.fromString(id));
    }

    public UUID getId() {
        return id;
    }

    public static CartId of(UUID id) {
        return new CartId(id);
    }

    public static CartId of(String id) {
        return new CartId(UUID.fromString(id));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CartId that)) return false;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return id.toString();
    }
}

