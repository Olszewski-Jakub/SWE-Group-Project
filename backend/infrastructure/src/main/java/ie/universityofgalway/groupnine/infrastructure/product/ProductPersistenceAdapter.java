package ie.universityofgalway.groupnine.infrastructure.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import ie.universityofgalway.groupnine.domain.product.*;
import ie.universityofgalway.groupnine.service.product.ProductPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Currency;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter that implements the ProductPort and connects the domain logic
 * to the persistence layer (JPA). It handles the conversion between
 * domain models and persistence entities.
 */
@Component
public class ProductPersistenceAdapter implements ProductPort {

    private final ProductJpaRepository repository;

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final double FUZZY_MATCH_SIMILARITY_THRESHOLD = 0.14;

    /**
     * Constructs the adapter with a product repository.
     *
     * @param repository The JPA repository for product data access.
     */
    public ProductPersistenceAdapter(ProductJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<Product> findAvailable(Pageable pageable) {
        return repository.findByAvailableTrue(pageable).map(this::toDomain);
    }

    @Override
    public Page<Product> findAvailableByCategory(String category, Pageable pageable) {
        return repository.findByCategoryIgnoreCaseAndAvailableTrue(category, pageable).map(this::toDomain);
    }

    /**
     * Executes a product search using nullable price bounds derived from the incoming query.
     * Normalization:
     *  Treats 0 as an unset minimum (converted to null) for this query pathway.
     *  Treats Integer.MAX_VALUE as an unset maximum (converted to null) for this query pathway.
     * Delegation:
     *  Passes normalized values to the repository method, which applies null-safe guards in JPQL.
     *  Builds a compact JSON object string for attributeFilters, shaped as: {"color":["Red","blue"],"size":["M","L"]}
     * Parameters:
     * @param searchQuery aggregate of user-provided filters (category, key, price bounds)
     * @param pageable    pagination and sorting information
     * @return a page of domain Product objects mapped from matching ProductEntity rows
     */
    @Override
    public Page<Product> search(SearchQuery searchQuery, Pageable pageable) {
        Integer minPriceCents = (searchQuery.minPriceCents() == 0) ? null : searchQuery.minPriceCents();
        Integer maxPriceCents = (searchQuery.maxPriceCents() == Integer.MAX_VALUE) ? null : searchQuery.maxPriceCents();
        String sort = String.valueOf(searchQuery.sortRule());

        // Build JSON for attribute filters only when provided; otherwise pass null to skip JSONB predicate.
        String attrJson = null;
        if (searchQuery.attributeFilters() != null && !searchQuery.attributeFilters().isEmpty()) {
            var map = searchQuery.attributeFilters().stream()
                    .collect(Collectors.toMap(AttributeFilter::name, AttributeFilter::values));
            try {
                attrJson = MAPPER.writeValueAsString(map);
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid attribute filters", e);
            }
        }
        return repository.search(
                searchQuery.category(),
                searchQuery.key(),
                minPriceCents,
                maxPriceCents,
                sort,
                FUZZY_MATCH_SIMILARITY_THRESHOLD, // similarity cutoff for fuzzy name/description match (pg_trgm)
                attrJson, // JSONB attribute filter blob; null disables attribute filtering
                pageable).map(this::toDomain);
    }

    @Override
    public Optional<Product> findById(ProductId id) {
        return repository.findByUuid(id.getId()).map(this::toDomain);
    }

    /**
     * Maps a VariantEntity from the persistence layer to a Variant domain object.
     *
     * @param variantEntity The entity to map.
     * @return The corresponding Variant domain object.
     */
    private Variant toVariantDomain(VariantEntity variantEntity) {
        String currencyCode = variantEntity.getCurrency();
        Currency currency = currencyCode != null ? Currency.getInstance(currencyCode) : Currency.getInstance("EUR");
        return new Variant(
                new VariantId(variantEntity.getUuid()),
                new Sku(variantEntity.getSku()),
                new Money(
                        BigDecimal.valueOf(variantEntity.getPriceCents() / 100.0),
                        currency
                ),
                new Stock(variantEntity.getStockQuantity(), variantEntity.getReservedQuantity()),
                Collections.emptyList() // Placeholder for images/options
        );
    }

    /**
     * Maps a ProductEntity from the persistence layer to a Product domain object.
     *
     * @param entity The entity to map.
     * @return The corresponding Product domain object.
     */
    private Product toDomain(ProductEntity entity) {
        // A product is ACTIVE if any of its variants are available

        ProductStatus status = entity.getVariants().stream().anyMatch(VariantEntity::isAvailable)
                ? ProductStatus.ACTIVE
                : ProductStatus.DRAFT;

        return new Product(
                new ProductId(entity.getUuid()),
                entity.getName(),
                entity.getDescription(),
                entity.getCategory(),
                status,
                entity.getVariants().stream()
                        .map(this::toVariantDomain)
                        .collect(Collectors.toList()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}