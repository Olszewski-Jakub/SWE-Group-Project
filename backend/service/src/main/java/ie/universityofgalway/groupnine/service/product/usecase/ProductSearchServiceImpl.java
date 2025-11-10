package ie.universityofgalway.groupnine.service.product.usecase;

import ie.universityofgalway.groupnine.domain.product.Product;
import ie.universityofgalway.groupnine.domain.product.SearchQuery;
import ie.universityofgalway.groupnine.domain.product.SortRule;
import ie.universityofgalway.groupnine.service.product.port.ProductPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/**

 Default search implementation using the product port/repository.
 */
@Service
public class ProductSearchServiceImpl implements ProductSearchService {

    private final ProductPort productPort;

    /**

     Creates the service.

     @param productPort data access adapter for product queries
     */
    public ProductSearchServiceImpl(ProductPort productPort) {
        this.productPort = productPort;
    }

    /** {@inheritDoc} */
    @Override
    public Page<Product> search(SearchQuery query, int page, int size) {
        validate(query);
        var pageable = org.springframework.data.domain.PageRequest.of(page, size);
        return productPort.search(query,pageable);
    }

    /**

     Basic request checks independent of persistence.

     @param req incoming request
     */
    private void validate(SearchQuery req) {
        if (req == null) throw new IllegalArgumentException("request must not be null");
        if (req.minPriceCents() < 0 || req.maxPriceCents() < 0)
            throw new IllegalArgumentException("price must be non‑negative");
        if (req.minPriceCents() > req.maxPriceCents())
            throw new IllegalArgumentException("minPriceCents must be <= maxPriceCents");
    }
}