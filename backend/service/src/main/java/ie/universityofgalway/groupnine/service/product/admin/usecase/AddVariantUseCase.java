package ie.universityofgalway.groupnine.service.product.admin.usecase;

import ie.universityofgalway.groupnine.domain.product.*;
import ie.universityofgalway.groupnine.service.product.port.ProductPort;

public class AddVariantUseCase {
    private final ProductPort port;
    public AddVariantUseCase(ProductPort port) { this.port = port; }

    public Variant execute(ProductId productId, Variant variant) {
        port.findById(productId).orElseThrow(() -> new java.util.NoSuchElementException("Product not found"));
        // Generate id if missing
        Variant v = variant;
        if (variant.getId() == null || variant.getId().getId() == null) {
            v = new Variant(new VariantId(java.util.UUID.randomUUID()), variant.getSku(), variant.getPrice(), variant.getStock(), variant.getAttributes());
        }
        validate(v);
        return port.saveVariant(productId, v);
    }

    private void validate(Variant v) {
        if (v == null) throw new IllegalArgumentException("variant required");
        if (v.getSku() == null || v.getSku().getValue() == null || v.getSku().getValue().isBlank()) {
            throw new IllegalArgumentException("sku must not be blank");
        }
        if (v.getPrice() == null || v.getPrice().getAmount() == null || v.getPrice().getAmount().signum() < 0) {
            throw new IllegalArgumentException("amount must be >= 0");
        }
        if (v.getPrice().getCurrency() == null) {
            throw new IllegalArgumentException("currency is required");
        }
        if (v.getStock() == null || v.getStock().getQuantity() < 0 || v.getStock().getReserved() < 0) {
            throw new IllegalArgumentException("stock must be >= 0");
        }
        if (v.getStock().getReserved() > v.getStock().getQuantity()) {
            throw new IllegalArgumentException("reserved cannot exceed quantity");
        }
    }
}
