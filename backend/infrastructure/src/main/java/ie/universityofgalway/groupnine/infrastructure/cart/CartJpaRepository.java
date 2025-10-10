package ie.universityofgalway.groupnine.infrastructure.cart;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for managing {@link ShoppingCartEntity} persistence.
 * <p>
 * Provides CRUD operations and an optional convenience method for retrieving carts by UUID.
 */
public interface CartJpaRepository extends JpaRepository<ShoppingCartEntity, UUID> {

    /**
     * Finds a shopping cart entity by its UUID.
     *
     * @param uuid the unique identifier of the shopping cart
     * @return an {@link Optional} containing the entity if found
     */
    Optional<ShoppingCartEntity> findByUuid(UUID uuid);
}

