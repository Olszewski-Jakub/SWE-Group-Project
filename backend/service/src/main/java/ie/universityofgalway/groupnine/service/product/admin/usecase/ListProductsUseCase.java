package ie.universityofgalway.groupnine.service.product.admin.usecase;

import ie.universityofgalway.groupnine.domain.product.Product;
import ie.universityofgalway.groupnine.service.product.port.ProductPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public class ListProductsUseCase {
    private final ProductPort port;
    public ListProductsUseCase(ProductPort port) { this.port = port; }
    public Page<Product> execute(Pageable pageable) { return port.listAll(pageable); }
}

