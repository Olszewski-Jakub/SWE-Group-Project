package ie.universityofgalway.groupnine.domain.inventory;

import java.util.Objects;
import java.util.UUID;

public final class InventoryReservationId {
    private final UUID value;

    public InventoryReservationId(UUID value) { this.value = Objects.requireNonNull(value); }

    public static InventoryReservationId newId() { return new InventoryReservationId(UUID.randomUUID()); }

    public static InventoryReservationId of(UUID value) { return new InventoryReservationId(value); }

    public UUID value() { return value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InventoryReservationId that)) return false;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() { return Objects.hash(value); }

    @Override
    public String toString() { return value.toString(); }
}

