package ie.universityofgalway.groupnine.infrastructure.product.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ie.universityofgalway.groupnine.domain.product.*;
import ie.universityofgalway.groupnine.infrastructure.product.jpa.ProductEntity;
import ie.universityofgalway.groupnine.infrastructure.product.jpa.ProductJpaRepository;
import ie.universityofgalway.groupnine.infrastructure.product.jpa.VariantEntity;
import ie.universityofgalway.groupnine.infrastructure.product.jpa.VariantJpaRepository;
import ie.universityofgalway.groupnine.service.product.port.ProductPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adapter that implements the ProductPort and connects the domain logic
 * to the persistence layer (JPA). It handles the conversion between
 * domain models and persistence entities.
 */
@Component
public class ProductPersistenceAdapter implements ProductPort {

    private final ProductJpaRepository repository;
    private final VariantJpaRepository variantRepository;

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final double FUZZY_MATCH_SIMILARITY_THRESHOLD = 0.14;

    /**
     * Constructs the adapter with a product repository.
     *
     * @param repository The JPA repository for product data access.
     */
    @Autowired
    public ProductPersistenceAdapter(ProductJpaRepository repository, VariantJpaRepository variantRepository) {
        this.repository = repository;
        this.variantRepository = variantRepository;
    }

    // Convenience constructor for tests that only need read/search behavior
    public ProductPersistenceAdapter(ProductJpaRepository repository) {
        this(repository, null);
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

    @Override
    public Optional<Product> findByVariantId(VariantId id) {
        return repository.findByVariantUuid(id.getId()).map(this::toDomain);
    }

    // ---------- Admin CRUD ----------
    @Override
    public Page<Product> listAll(Pageable pageable) { return repository.findAll(pageable).map(this::toDomain); }

    @Override
    public boolean productExistsByUuid(UUID uuid) { return repository.existsByUuid(uuid); }

    @Override
    @Transactional
    public Product saveProduct(Product product) {
        ProductEntity entity = repository.findByUuid(product.getId().getId()).orElseGet(ProductEntity::new);
        if (entity.getUuid() == null) setField(entity, "uuid", product.getId().getId());
        entity.setName(product.getName());
        entity.setDescription(product.getDescription());
        entity.setCategory(product.getCategory());
        entity.setStatus(product.getStatus().name());

        // Merge variants in place to avoid replacing the collection (works with orphanRemoval)
        List<VariantEntity> current = entity.getVariants() == null ? new ArrayList<>() : entity.getVariants();
        java.util.Map<java.util.UUID, VariantEntity> existingByUuid = new java.util.HashMap<>();
        for (VariantEntity ve : current) {
            if (ve.getUuid() != null) existingByUuid.put(ve.getUuid(), ve);
        }

        java.util.Set<java.util.UUID> keep = new java.util.HashSet<>();
        for (Variant v : product.getVariants()) {
            UUID vUuid = (v.getId() == null) ? null : v.getId().getId();
            VariantEntity ve = (vUuid != null) ? existingByUuid.get(vUuid) : null;
            if (ve == null) {
                ve = new VariantEntity();
                setField(ve, "uuid", vUuid == null ? java.util.UUID.randomUUID() : vUuid);
                ve.setProduct(entity);
                current.add(ve);
            }
            ve.setSku(v.getSku().getValue());
            ve.setPriceCents(amountToCents(v.getPrice().getAmount()));
            ve.setCurrency(v.getPrice().getCurrency().getCurrencyCode());
            ve.setStockQuantity(v.getStock().getQuantity());
            ve.setReservedQuantity(v.getStock().getReserved());
            ve.setAttributes(attributesToJson(v.getAttributes()));
            ve.setImageUrl(v.getImageUrl());
            if (ve.getUuid() != null) keep.add(ve.getUuid());
        }

        // Remove variants not present in incoming list (orphanRemoval will delete them)
        current.removeIf(ve -> ve.getUuid() != null && !keep.contains(ve.getUuid()));
        ProductEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    @Transactional
    public void deleteProduct(ProductId id) {
        repository.findByUuid(id.getId()).ifPresent(e -> repository.deleteById(e.getId()));
    }

    @Override
    public boolean variantExistsBySku(String sku) { return variantRepository.existsBySku(sku); }

    @Override
    public Optional<Variant> findVariantById(VariantId id) {
        return variantRepository.findByUuid(id.getId()).map(this::toVariantDomain);
    }

    @Override
    @Transactional
    public Variant saveVariant(ProductId productId, Variant variant) {
        ProductEntity product = repository.findByUuid(productId.getId()).orElseThrow();
        VariantEntity entity = variantRepository.findByUuid(variant.getId().getId()).orElseGet(VariantEntity::new);
        if (entity.getUuid() == null) setField(entity, "uuid", variant.getId().getId());
        entity.setProduct(product);
        entity.setSku(variant.getSku().getValue());
        entity.setPriceCents(amountToCents(variant.getPrice().getAmount()));
        entity.setCurrency(variant.getPrice().getCurrency().getCurrencyCode());
        entity.setStockQuantity(variant.getStock().getQuantity());
        entity.setReservedQuantity(variant.getStock().getReserved());
        entity.setAttributes(attributesToJson(variant.getAttributes()));
        entity.setImageUrl(variant.getImageUrl());
        VariantEntity saved = variantRepository.save(entity);
        return toVariantDomain(saved);
    }

    @Override
    @Transactional
    public void deleteVariant(ProductId productId, VariantId variantId) {
        variantRepository.findByUuid(variantId.getId()).ifPresent(variantRepository::delete);
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
        BigDecimal amount = BigDecimal.valueOf(variantEntity.getPriceCents()).divide(BigDecimal.valueOf(100));
        return new Variant(
                new VariantId(variantEntity.getUuid()),
                new Sku(variantEntity.getSku()),
                new Money(amount, currency),
                new Stock(variantEntity.getStockQuantity(), variantEntity.getReservedQuantity()),
                jsonToAttributes(variantEntity.getAttributes()),
                variantEntity.getImageUrl()
        );
    }

    /**
     * Maps a ProductEntity from the persistence layer to a Product domain object.
     *
     * @param entity The entity to map.
     * @return The corresponding Product domain object.
     */
    private Product toDomain(ProductEntity entity) {
        // For read mapping, consider product ACTIVE if any variant is available; otherwise DRAFT
        ProductStatus status = (entity.getVariants() != null && entity.getVariants().stream().anyMatch(VariantEntity::isAvailable))
                ? ProductStatus.ACTIVE
                : ProductStatus.DRAFT;
        return new Product(
                new ProductId(entity.getUuid()),
                entity.getName(),
                entity.getDescription(),
                entity.getCategory(),
                status,
                entity.getVariants().stream().map(this::toVariantDomain).toList(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private int amountToCents(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).intValueExact();
    }

    private JsonNode attributesToJson(List<Attribute> attributes) {
        if (attributes == null || attributes.isEmpty()) return null;
        ObjectNode obj = MAPPER.createObjectNode();
        attributes.stream().collect(Collectors.groupingBy(Attribute::name))
                .forEach((name, list) -> {
                    if (list.size() == 1) obj.put(name, list.get(0).value());
                    else {
                        ArrayNode arr = MAPPER.createArrayNode();
                        list.forEach(a -> arr.add(a.value()));
                        obj.set(name, arr);
                    }
                });
        return obj;
    }

    private List<Attribute> jsonToAttributes(JsonNode node) {
        if (node == null || node.isNull()) return List.of();
        List<Attribute> list = new ArrayList<>();
        node.fieldNames().forEachRemaining(fn -> {
            JsonNode v = node.get(fn);
            if (v.isArray()) v.forEach(el -> list.add(new Attribute(fn, el.asText())));
            else list.add(new Attribute(fn, v.asText()));
        });
        return list;
    }

    private void setField(Object target, String field, Object value) {
        try {
            java.lang.reflect.Field f = target.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception ignored) {}
    }

    private VariantEntity toEntity(Variant v) {
        VariantEntity e = new VariantEntity();
        setField(e, "uuid", v.getId().getId());
        e.setSku(v.getSku().getValue());
        e.setPriceCents(amountToCents(v.getPrice().getAmount()));
        e.setCurrency(v.getPrice().getCurrency().getCurrencyCode());
        e.setStockQuantity(v.getStock().getQuantity());
        e.setReservedQuantity(v.getStock().getReserved());
        e.setAttributes(attributesToJson(v.getAttributes()));
        e.setImageUrl(v.getImageUrl());
        return e;
    }
}
