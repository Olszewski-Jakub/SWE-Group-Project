package ie.universityofgalway.groupnine.domain.inventory;

/**
 * States for inventory reservation lifecycle.
 */
public enum InventoryReservationStatus {
    PENDING,
    RESERVED,
    CONFIRMED,
    RELEASED,
    EXPIRED
}

