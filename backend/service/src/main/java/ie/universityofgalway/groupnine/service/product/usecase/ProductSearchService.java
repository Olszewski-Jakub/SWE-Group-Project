package ie.universityofgalway.groupnine.service.product.usecase;

import ie.universityofgalway.groupnine.domain.product.Product;
import ie.universityofgalway.groupnine.domain.product.SearchQuery;
import org.springframework.data.domain.Page;

/**
 * Application service contract for searching and filtering products.
 * <p>
 * Exposes a single use case that accepts a {@link SearchQuery} and returns
 * a paged, sorted slice of matching {@link Product} entities. Implementations
 * may delegate to repositories, query adapters, or external services while
 * preserving the contract for callers (e.g., REST controllers).
 */
public interface ProductSearchService {

    /**
     * Executes a product search using the provided filters and sort rule.
     *
     * @param request criteria for keyword, category, price range, attributes, and sort
     * @param page    zero-based page index to return (must be {@code >= 0})
     * @param size    number of items per page (must be {@code > 0}); implementations may cap this to a safe maximum
     * @return a page of matching products, ordered according to the request’s sort rule
     * @throws IllegalArgumentException if the request contains invalid bounds (e.g., minPrice > maxPrice)
     */
    Page<Product> search(SearchQuery request, int page, int size);
}