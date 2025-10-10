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
 * <p>
 * Responsible for mapping between domain models ({@link ShoppingCart}, {@link CartItem})
 * and persistence entities ({@link ShoppingCartEntity}, {@link CartItemEntity}).
 * <p>
 * This class forms the bridge between the domain and infrastructure layers
 * following the hexagonal architecture (ports and adapters) pattern.
 */
@Component
public class CartPersistenceAdapter implements ShoppingCartPort {

    private final CartJpaRepository repository;
    private final VariantPort variantPort;

    /**
     * Constructs a persistence adapter with dependencies.
     *
     * @param repository  Spring Data JPA repository for carts
     * @param variantPort domain port for accessing product variants
     */
    public CartPersistenceAdapter(CartJpaRepository repository, VariantPort variantPort) {
        this.repository = repository;
        this.variantPort = variantPort;
    }

    /**
     * Finds a shopping cart by its domain {@link CartId}.
     *
     * @param id the cart identifier
     * @return an {@link Optional} containing the domain cart, or empty if not found
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<ShoppingCart> findById(CartId id) {
        return repository.findById(id.id()).map(this::toDomain);
    }

    /**
     * Persists or updates a shopping cart and its items.
     * <p>
     * Clears existing items in the entity before saving the new ones from the domain model.
     *
     * @param cart the domain shopping cart to persist
     * @return the same cart instance
     */
    @Override
    @Transactional
    public ShoppingCart save(ShoppingCart cart) {
        ShoppingCartEntity entity = repository.findById(cart.id().id())
                .orElseGet(() -> new ShoppingCartEntity(cart.id().id(), cart.userId().value()));

        entity.setUserId(cart.userId().value());
        entity.setStatus(cart.status());
        entity.setUpdatedAt(Instant.now().toEpochMilli());

        // Sync items
        entity.clearItems();
        for (CartItem item : cart.items().asList()) {
            Variant variant = item.variant();
            entity.addItem(variant.id().id(), item.quantity());
        }

        repository.save(entity);
        return cart;
    }

    /**
     * Deletes a shopping cart by its identifier.
     *
     * @param id the cart identifier
     */
    @Override
    @Transactional
    public void delete(CartId id) {
        repository.findById(id.id()).ifPresent(repository::delete);
    }

    /**
     * Converts a JPA {@link ShoppingCartEntity} to a domain {@link ShoppingCart}.
     * <p>
     * Fetches variant information via {@link VariantPort}.
     *
     * @param entity the persistence entity
     * @return domain model representation
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