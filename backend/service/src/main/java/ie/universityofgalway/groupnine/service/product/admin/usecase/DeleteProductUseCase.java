package ie.universityofgalway.groupnine.service.product.admin.usecase;

import ie.universityofgalway.groupnine.domain.product.ProductId;
import ie.universityofgalway.groupnine.service.product.port.ProductPort;

public class DeleteProductUseCase {
    private final ProductPort port;
    public DeleteProductUseCase(ProductPort port) { this.port = port; }
    public void execute(ProductId id) {
        if (port.findById(id).isEmpty()) throw new java.util.NoSuchElementException("Product not found");
        port.deleteProduct(id);
    }
}

