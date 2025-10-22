package ie.universityofgalway.groupnine.domain.cart;

/**
 * Domain exception thrown when attempting to add more items
 * to a cart than are available in stock.
 */
public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) {
        super(message);
    }
}

