package ie.universityofgalway.groupnine.service.product.admin;

public class SkuConflictException extends RuntimeException {
    public SkuConflictException(String sku) {
        super("SKU already exists: " + sku);
    }
}

