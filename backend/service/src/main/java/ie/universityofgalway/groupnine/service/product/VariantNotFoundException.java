package ie.universityofgalway.groupnine.service.product;

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
