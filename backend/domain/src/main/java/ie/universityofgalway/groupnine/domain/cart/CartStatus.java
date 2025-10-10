package ie.universityofgalway.groupnine.domain.cart;

/**
 * Represents the lifecycle state of a {@link ShoppingCart}.
 */
public enum CartStatus {
    /** Cart is active and can be modified. */
    ACTIVE,
    /** Customer has checked out; cart is locked. */
    CHECKED_OUT,
    /** Cart expired or abandoned without checkout. */
    ABANDONED
}
