package ie.universityofgalway.groupnine.domain.inventory;

import ie.universityofgalway.groupnine.domain.order.OrderId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Tracks a soft reservation of inventory items for an order with TTL.
 */
public final class InventoryReservation {
    private final InventoryReservationId id;
    private final OrderId orderId;
    private final List<ReservationItem> items;
    private InventoryReservationStatus status;
    private final Instant createdAt;
    private Instant updatedAt;
    private Instant expiresAt;

    public InventoryReservation(InventoryReservationId id, OrderId orderId, List<ReservationItem> items,
                                InventoryReservationStatus status, Instant createdAt, Instant updatedAt,
                                Instant expiresAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.orderId = Objects.requireNonNull(orderId, "orderId");
        this.items = List.copyOf(Objects.requireNonNull(items, "items"));
        this.status = Objects.requireNonNull(status, "status");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
        this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt");
    }

    public static InventoryReservation pending(OrderId orderId, List<ReservationItem> items, Instant expiresAt) {
        Instant now = Instant.now();
        return new InventoryReservation(InventoryReservationId.newId(), orderId, new ArrayList<>(items),
                InventoryReservationStatus.PENDING, now, now, expiresAt);
    }

    public InventoryReservationId getId() { return id; }
    public OrderId getOrderId() { return orderId; }
    public List<ReservationItem> getItems() { return List.copyOf(items); }
    public InventoryReservationStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getExpiresAt() { return expiresAt; }

    public void markReserved() {
        ensureOneOf(InventoryReservationStatus.PENDING);
        this.status = InventoryReservationStatus.RESERVED;
        touch();
    }

    public void confirm() {
        ensureOneOf(InventoryReservationStatus.RESERVED);
        this.status = InventoryReservationStatus.CONFIRMED;
        touch();
    }

    public void release() {
        ensureOneOf(InventoryReservationStatus.PENDING, InventoryReservationStatus.RESERVED);
        this.status = InventoryReservationStatus.RELEASED;
        touch();
    }

    public void expire() {
        ensureOneOf(InventoryReservationStatus.PENDING, InventoryReservationStatus.RESERVED);
        this.status = InventoryReservationStatus.EXPIRED;
        touch();
    }

    private void ensureOneOf(InventoryReservationStatus... allowed) {
        for (InventoryReservationStatus s : allowed) {
            if (this.status == s) return;
        }
        throw new IllegalStateException("Invalid transition from " + this.status);
    }

    private void touch() { this.updatedAt = Instant.now(); }
}

