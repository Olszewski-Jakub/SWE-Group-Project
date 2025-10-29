package ie.universityofgalway.groupnine.domain.cart.exception;

/**
 * Exception thrown when a cart is not found.
 */
public class CartNotFoundException extends RuntimeException {
    public CartNotFoundException(String id) {
        super("Cart " + id + " not found");
    }
}