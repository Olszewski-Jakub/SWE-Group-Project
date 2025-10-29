package ie.universityofgalway.groupnine.service.product.admin.usecase;

import ie.universityofgalway.groupnine.domain.product.Product;
import ie.universityofgalway.groupnine.domain.product.ProductId;
import ie.universityofgalway.groupnine.service.product.port.ProductPort;

import java.util.Optional;

public class GetProductUseCase {
    private final ProductPort port;
    public GetProductUseCase(ProductPort port) { this.port = port; }
    public Optional<Product> byId(ProductId id) { return port.findById(id); }
}

