package ie.universityofgalway.groupnine.service.product.usecase;

import java.util.NoSuchElementException;
import java.util.UUID;

import ie.universityofgalway.groupnine.service.product.port.ProductPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import ie.universityofgalway.groupnine.domain.product.Product;
import ie.universityofgalway.groupnine.domain.product.ProductId;


/**
 * Application service for product queries.
 * 
 * Orchestrates paging, filtering, and lookup behavior over the ProductPort
 * while enforcing application-level constraints (argument validation, error
 * mapping, and ID parsing).
 */
@Service
public class ProductService {

    private final ProductPort productPort;

    /**
     * Constructs the service.
     */
    public ProductService(ProductPort productPort) {
        this.productPort = productPort;
    }

    /**
     * Lists products that are available, optionally filtered by category.
     */
    public Page<Product> list(int page, int size, String category) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        if (category == null || category.isBlank()) {
            return productPort.findAvailable(pageable);
        } else {
            return productPort.findAvailableByCategory(category.trim(), pageable);
        }
    }
    /**
     * Resolves a product by its public UUID string identifier.
     */
    public Product getById(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        final UUID uuid;
        try {
            uuid = UUID.fromString(id.trim());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid UUID format: " + id);
        }

        return productPort
                .findById(new ProductId(uuid))
                .orElseThrow(() -> new NoSuchElementException("Product not found: " + id));
    }
}
