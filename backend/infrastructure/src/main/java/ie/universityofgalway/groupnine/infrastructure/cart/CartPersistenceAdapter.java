package ie.universityofgalway.groupnine.infrastructure.cart;

import ie.universityofgalway.groupnine.domain.cart.*;
import ie.universityofgalway.groupnine.domain.product.Variant;
import ie.universityofgalway.groupnine.domain.product.VariantId;
import ie.universityofgalway.groupnine.domain.user.UserId;
import ie.universityofgalway.groupnine.service.cart.ShoppingCartPort;
import ie.universityofgalway.groupnine.service.product.VariantPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/**
 * Persistence adapter implementing {@link ShoppingCartPort} using JPA.
 *
 * <p>This adapter handles the mapping between the domain models ({@link ShoppingCart}, {@link CartItem})
 * and the persistence entities ({@link ShoppingCartEntity}, {@link CartItemEntity}).</p>
 *
 * <p>It serves as the bridge between the domain layer and the infrastructure layer, following
 * the hexagonal architecture (ports and adapters) pattern.</p>
 */
@Component
public class CartPersistenceAdapter implements ShoppingCartPort {

    private final CartJpaRepository repository;
    private final VariantPort variantPort;

    /**
     * Constructs a new {@code CartPersistenceAdapter} with the required dependencies.
     *
     * @param repository  the Spring Data JPA repository used to manage cart entities
     * @param variantPort the domain port for retrieving {@link Variant} objects
     */
    public CartPersistenceAdapter(CartJpaRepository repository, VariantPort variantPort) {
        this.repository = repository;
        this.variantPort = variantPort;
    }

    /**
     * Finds a shopping cart for a specific user.
     *
     * @param userId the {@link UserId} of the user
     * @return an {@link Optional} containing the domain {@link ShoppingCart} if found,
     *         otherwise an empty {@link Optional}
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<ShoppingCart> findByUserId(UserId userId) {
        return repository.findByUserId(userId.value()).map(this::toDomain);
    }

    /**
     * Finds a shopping cart by its {@link CartId}.
     *
     * @param id the domain cart identifier
     * @return an {@link Optional} containing the domain {@link ShoppingCart} if found,
     *         otherwise an empty {@link Optional}
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<ShoppingCart> findById(CartId id) {
        return repository.findById(id.getId()).map(this::toDomain);
    }

    /**
     * Persists or updates a shopping cart.
     *
     * <p>If the cart already exists, it updates the existing entity; otherwise, it creates a new one.
     * Items are synchronized by clearing the entity's current items and adding all items from the
     * domain cart.</p>
     *
     * @param cart the domain {@link ShoppingCart} to save
     * @return the same domain cart instance
     */
    @Override
    @Transactional
    public ShoppingCart save(ShoppingCart cart) {
        ShoppingCartEntity entity = repository.findById(cart.id().getId())
                .orElseGet(() -> new ShoppingCartEntity(cart.id().getId(), cart.userId().value()));

        entity.setUserId(cart.userId().value());
        entity.setStatus(cart.status());
        entity.setUpdatedAt(Instant.now().toEpochMilli());

        // Synchronize items
        entity.clearItems();
        for (CartItem item : cart.items().asList()) {
            Variant variant = item.getVariant();
            entity.addItem(variant.id().id(), item.getQuantity());
        }

        repository.save(entity);
        return cart;
    }

    /**
     * Deletes a shopping cart by its {@link CartId}.
     *
     * @param id the domain cart identifier
     */
    @Override
    @Transactional
    public void delete(CartId id) {
        repository.findById(id.getId()).ifPresent(repository::delete);
    }

    /**
     * Converts a {@link ShoppingCartEntity} to a domain {@link ShoppingCart}.
     *
     * <p>Variant information is fetched using the {@link VariantPort}. Throws a
     * {@link RuntimeException} if any variant cannot be found.</p>
     *
     * @param entity the persistence entity
     * @return the corresponding domain {@link ShoppingCart}
     */
    private ShoppingCart toDomain(ShoppingCartEntity entity) {
        CartItems cartItems = new CartItems();

        for (CartItemEntity itemEntity : entity.getItems()) {
            Variant variant = variantPort.findById(new VariantId(itemEntity.getVariantId()))
                    .orElseThrow(() -> new RuntimeException(
                            "Variant not found: " + itemEntity.getVariantId()
                    ));
            cartItems.add(variant, itemEntity.getQuantity());
        }

        return new ShoppingCart(
                new CartId(entity.getUuid()),
                UserId.of(entity.getUserId()),
                cartItems,
                entity.getStatus(),
                Instant.ofEpochMilli(entity.getCreatedAt()),
                Instant.ofEpochMilli(entity.getUpdatedAt())
        );
    }
}
