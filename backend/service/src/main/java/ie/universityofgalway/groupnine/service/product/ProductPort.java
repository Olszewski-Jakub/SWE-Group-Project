package ie.universityofgalway.groupnine.service.product;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import ie.universityofgalway.groupnine.domain.product.Product;
import ie.universityofgalway.groupnine.domain.product.ProductId;

/**
 * This port exposes read-only access patterns needed by the application
 * layer (e.g. REST controllers). Implementations (e.g. JPA, in-memory)
 * are provided in the infrastructure layer and must enforce the same
 * semantics documented here.
 */
public interface ProductPort {
    /**
     * Returns a page of products that have at least one available variant.
     */
    Page<Product> findAvailable(Pageable pageable);

    /**
     * Returns a page of products in the given category that have at least
     * one available variant.
     */
    Page<Product> findAvailableByCategory(String category, Pageable pageable);

    /**
     * Finds a product by its public identifier.
     */
    Optional<Product> findById(ProductId id);
}
