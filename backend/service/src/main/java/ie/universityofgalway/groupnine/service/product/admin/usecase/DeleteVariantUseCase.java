package ie.universityofgalway.groupnine.service.product.admin.usecase;

import ie.universityofgalway.groupnine.domain.product.ProductId;
import ie.universityofgalway.groupnine.domain.product.VariantId;
import ie.universityofgalway.groupnine.service.product.port.ProductPort;

public class DeleteVariantUseCase {
    private final ProductPort port;
    public DeleteVariantUseCase(ProductPort port) { this.port = port; }
    public void execute(ProductId productId, VariantId variantId) {
        if (port.findById(productId).isEmpty()) throw new java.util.NoSuchElementException("Product not found");
        if (port.findVariantById(variantId).isEmpty()) throw new java.util.NoSuchElementException("Variant not found");
        port.deleteVariant(productId, variantId);
    }
}

