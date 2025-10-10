package ie.universityofgalway.groupnine.infrastructure.product;

import ie.universityofgalway.groupnine.domain.product.*;
import ie.universityofgalway.groupnine.service.product.VariantPort;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Currency;
import java.util.Optional;

/**
 * Persistence adapter implementing {@link VariantPort} using JPA.
 * Maps between domain {@link Variant} and persistence {@link VariantEntity}.
 */
@Component
public class VariantPersistenceAdapter implements VariantPort {

    private final VariantJpaRepository repository;

    public VariantPersistenceAdapter(VariantJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Variant> findById(VariantId id) {
        return repository.findByUuid(id.id())
                .map(this::toDomain);
    }

    /**
     * Converts a VariantEntity to a domain Variant object.
     */
    private Variant toDomain(VariantEntity entity) {
        Currency currency = entity.getCurrency() != null
                ? Currency.getInstance(entity.getCurrency())
                : Currency.getInstance("EUR");

        return new Variant(
                new VariantId(entity.getUuid()),         // 1. VariantId
                new Sku(entity.getSku()),                // 2. SKU
                new Money(BigDecimal.valueOf(entity.getPriceCents() / 100.0), currency), // 3. Price
                new Stock(entity.getStockQuantity(), entity.getReservedQuantity()),      // 4. Stock
                Collections.emptyList()                  // 5. Attributes (use empty list if none)
        );
    }}

