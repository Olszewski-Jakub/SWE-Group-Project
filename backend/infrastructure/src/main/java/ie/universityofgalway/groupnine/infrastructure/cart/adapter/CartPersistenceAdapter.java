package ie.universityofgalway.groupnine.infrastructure.cart.adapter;

import ie.universityofgalway.groupnine.domain.cart.*;
import ie.universityofgalway.groupnine.domain.product.Variant;
import ie.universityofgalway.groupnine.domain.product.VariantId;
import ie.universityofgalway.groupnine.domain.user.UserId;
import ie.universityofgalway.groupnine.infrastructure.cart.jpa.CartItemEntity;
import ie.universityofgalway.groupnine.infrastructure.cart.jpa.CartJpaRepository;
import ie.universityofgalway.groupnine.infrastructure.cart.jpa.ShoppingCartEntity;
import ie.universityofgalway.groupnine.service.cart.port.ShoppingCartPort;
import ie.universityofgalway.groupnine.service.product.port.VariantPort;
import java.time.Instant;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implements the {@link ShoppingCartPort} to provide a persistence mechanism for
 * {@link ShoppingCart} aggregates using JPA. It manages the lifecycle and mapping
 * between the domain model and the {@link ShoppingCartEntity} persistence model.
 */
@Component
public class CartPersistenceAdapter implements ShoppingCartPort {

    private final CartJpaRepository repository;
    private final VariantPort variantPort;

    /**
     * Constructs a new CartPersistenceAdapter.
     *
     * @param repository The JPA repository for shopping cart entities.
     * @param variantPort The port for fetching variant domain objects.
     */
    public CartPersistenceAdapter(CartJpaRepository repository, VariantPort variantPort) {
        this.repository = repository;
        this.variantPort = variantPort;
    }

    /**
     * Finds a user's shopping cart by their unique user identifier.
     *
     * @param userId The {@link UserId} of the cart's owner.
     * @return An {@link Optional} containing the {@link ShoppingCart} if found,
     * or an empty Optional if not.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<ShoppingCart> findByUserId(UserId userId) {
        return repository.findFirstByUserIdAndStatusOrderByUpdatedAtDesc(userId.getId(), CartStatus.ACTIVE)
                .map(this::toDomain);
    }

    /**
     * Finds a shopping cart by its unique identifier.
     *
     * @param id The {@link CartId} of the cart to find.
     * @return An {@link Optional} containing the {@link ShoppingCart} if found,
     * or an empty Optional if not.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<ShoppingCart> findById(CartId id) {
        return repository.findById(id.getId()).map(this::toDomain);
    }

    /**
     * Saves or updates a {@link ShoppingCart} in the database. It maps the domain
     * aggregate to a persistence entity before saving.
     *
     * @param cart The {@link ShoppingCart} domain object to save.
     * @return The original, unmodified ShoppingCart domain object.
     */
    @Override
    @Transactional
    public ShoppingCart save(ShoppingCart cart) {
        ShoppingCartEntity entity = repository.findById(cart.getId().getId())
                .orElseGet(() -> new ShoppingCartEntity(cart.getId().getId(), cart.getUserId().getId()));

        entity.setUserId(cart.getUserId().getId());
        entity.setStatus(cart.getStatus());
        entity.setUpdatedAt(Instant.now().toEpochMilli());

        // Rebuild items with a delete-then-insert approach to avoid unique constraint conflicts
        entity.clearItems();
        // Flush deletions before re-inserting to satisfy (cart_uuid, variant_id) unique constraint
        repository.saveAndFlush(entity);
        for (CartItem item : cart.getItems()) {
            Variant variant = item.getVariant();
            entity.addItem(variant.getId().getId(), item.getQuantity());
        }

        repository.saveAndFlush(entity);
        return cart;
    }

    /**
     * Deletes a shopping cart from the database by its unique identifier.
     *
     * @param id The {@link CartId} of the cart to delete.
     */
    @Override
    @Transactional
    public void delete(CartId id) {
        repository.findById(id.getId()).ifPresent(repository::delete);
    }

    /**
     * Converts a {@link ShoppingCartEntity} to a {@link ShoppingCart} domain aggregate.
     * This includes reconstructing the {@link CartItems} by fetching each variant.
     *
     * @param entity The persistence entity to convert.
     * @return The corresponding domain aggregate.
     * @throws RuntimeException if a variant for an item cannot be found.
     */
    private ShoppingCart toDomain(ShoppingCartEntity entity) {
        CartItems cartItems = new CartItems();

        for (CartItemEntity itemEntity : entity.getItems()) {
             Variant variant = variantPort.findById(new VariantId(itemEntity.getVariantId()))
                    .orElseThrow(() -> new RuntimeException(
                            "Variant not found during cart hydration: " + itemEntity.getVariantId()
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
