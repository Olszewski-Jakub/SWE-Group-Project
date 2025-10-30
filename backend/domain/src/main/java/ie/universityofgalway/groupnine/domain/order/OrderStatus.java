package ie.universityofgalway.groupnine.domain.order;

/**
 * Lifecycle states for an Order.
 */
public enum OrderStatus {
    PENDING_PAYMENT,
    PAID,
    PAYMENT_FAILED,
    REFUNDED,
    CANCELLED
}

