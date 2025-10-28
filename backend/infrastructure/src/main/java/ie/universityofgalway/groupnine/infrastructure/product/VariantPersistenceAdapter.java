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
        // FIX: Changed id.id() to id.getId()
        return repository.findByUuid(id.getId())
                .map(this::toDomain);
    }

    /**
     * Converts a VariantEntity to a domain Variant object.
     * Assumes getters exist on VariantEntity.
     */
    private Variant toDomain(VariantEntity entity) {
        Currency currency = entity.getCurrency() != null
                ? Currency.getInstance(entity.getCurrency())
                : Currency.getInstance("EUR"); // Default currency

        return new Variant(
                new VariantId(entity.getUuid()),
                new Sku(entity.getSku()),
                new Money(BigDecimal.valueOf(entity.getPriceCents() / 100.0), currency),
                new Stock(entity.getStockQuantity(), entity.getReservedQuantity()),
                Collections.emptyList() // Assuming attributes are not mapped here
        );
    }
}
