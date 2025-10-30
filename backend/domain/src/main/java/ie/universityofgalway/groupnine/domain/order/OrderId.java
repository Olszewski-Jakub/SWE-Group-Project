package ie.universityofgalway.groupnine.domain.order;

import java.util.Objects;
import java.util.UUID;

/**
 * Value object representing the unique identifier of an Order.
 */
public final class OrderId {
    private final UUID value;

    public OrderId(UUID value) {
        this.value = Objects.requireNonNull(value, "value");
    }

    public static OrderId newId() { return new OrderId(UUID.randomUUID()); }

    public static OrderId of(UUID value) { return new OrderId(value); }

    public UUID value() { return value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderId orderId)) return false;
        return value.equals(orderId.value);
    }

    @Override
    public int hashCode() { return Objects.hash(value); }

    @Override
    public String toString() { return value.toString(); }
}

