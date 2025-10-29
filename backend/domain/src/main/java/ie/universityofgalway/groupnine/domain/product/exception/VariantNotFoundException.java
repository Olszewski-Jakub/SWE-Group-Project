package ie.universityofgalway.groupnine.domain.product.exception;

/**
 * Exception thrown when a requested product variant does not exist.
 */
public class VariantNotFoundException extends RuntimeException {

    public VariantNotFoundException(String message) {
        super(message);
    }

    public VariantNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
