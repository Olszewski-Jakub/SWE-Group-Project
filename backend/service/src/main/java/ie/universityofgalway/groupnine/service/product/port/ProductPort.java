package ie.universityofgalway.groupnine.service.product.port;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import ie.universityofgalway.groupnine.domain.product.Product;
import ie.universityofgalway.groupnine.domain.product.ProductId;
import ie.universityofgalway.groupnine.domain.product.SearchQuery;
import ie.universityofgalway.groupnine.domain.product.Variant;
import ie.universityofgalway.groupnine.domain.product.VariantId;

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
     * Searches products using the given criteria and pagination.
     */
    Page<Product> search(SearchQuery query, Pageable pageable);

    /**
     * Finds a product by its public identifier.
     */
    Optional<Product> findById(ProductId id);

    // Admin (CRUD) operations
    Page<Product> listAll(Pageable pageable);
    boolean productExistsByUuid(UUID uuid);
    Product saveProduct(Product product);
    void deleteProduct(ProductId id);

    boolean variantExistsBySku(String sku);
    Optional<Variant> findVariantById(VariantId id);
    Variant saveVariant(ProductId productId, Variant variant);
    void deleteVariant(ProductId productId, VariantId variantId);
}
