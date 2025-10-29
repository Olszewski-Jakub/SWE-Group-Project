package ie.universityofgalway.groupnine.infrastructure.product.adapter;

import ie.universityofgalway.groupnine.domain.product.*;
import ie.universityofgalway.groupnine.infrastructure.product.jpa.VariantEntity;
import ie.universityofgalway.groupnine.infrastructure.product.jpa.VariantJpaRepository;
import ie.universityofgalway.groupnine.service.product.port.VariantPort;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Currency;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Implements the {@link VariantPort} to provide a persistence mechanism for
 * {@link Variant} domain objects using JPA. It handles the mapping between
 * the domain model and the {@link VariantEntity} persistence model.
 */
@Component
public class VariantPersistenceAdapter implements VariantPort {

    private final VariantJpaRepository repository;

    /**
     * Constructs a new VariantPersistenceAdapter.
     *
     * @param repository The JPA repository for variant entities.
     */
    public VariantPersistenceAdapter(VariantJpaRepository repository) {
        this.repository = repository;
    }

    /**
     * Finds a product variant by its unique identifier.
     *
     * @param id The {@link VariantId} of the variant to find.
     * @return An {@link Optional} containing the {@link Variant} if found,
     * or an empty Optional if not.
     */
    @Override
    public Optional<Variant> findById(VariantId id) {
        return repository.findByUuid(id.getId()).map(this::toDomain);
    }

    /**
     * Converts a {@link VariantEntity} persistence object to a {@link Variant} domain object.
     *
     * @param entity The persistence entity to convert.
     * @return The corresponding domain object.
     */
    private Variant toDomain(VariantEntity entity) {
        Currency currency = entity.getCurrency() != null
                ? Currency.getInstance(entity.getCurrency())
                : Currency.getInstance("EUR");

        return new Variant(
                new VariantId(entity.getUuid()),
                new Sku(entity.getSku()),
                new Money(BigDecimal.valueOf(entity.getPriceCents() / 100.0), currency),
                new Stock(entity.getStockQuantity(), entity.getReservedQuantity()),
                Collections.emptyList()
        );
    }
}