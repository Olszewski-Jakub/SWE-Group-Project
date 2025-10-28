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
 */
@Component
public class CartPersistenceAdapter implements ShoppingCartPort {

    private final CartJpaRepository repository;
    private final VariantPort variantPort;

    public CartPersistenceAdapter(CartJpaRepository repository, VariantPort variantPort) {
        this.repository = repository;
        this.variantPort = variantPort;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ShoppingCart> findByUserId(UserId userId) {
        // *** FIX: Assuming UserId now has getId() returning UUID ***
        // Use the UUID from the UserId object to query the repository.
        return repository.findByUserId(userId.getId()).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ShoppingCart> findById(CartId id) {
        // This was already correct
        return repository.findById(id.getId()).map(this::toDomain);
    }

    @Override
    @Transactional
    public ShoppingCart save(ShoppingCart cart) {
        // Use the UUIDs from the domain ID objects
        ShoppingCartEntity entity = repository.findById(cart.getId().getId())
                // *** FIX: Assuming UserId has getId() returning UUID ***
                .orElseGet(() -> new ShoppingCartEntity(cart.getId().getId(), cart.getUserId().getId()));

        // *** FIX: Assuming UserId has getId() returning UUID ***
        entity.setUserId(cart.getUserId().getId());
        entity.setStatus(cart.getStatus());
        entity.setUpdatedAt(Instant.now().toEpochMilli()); // Can keep using Instant directly

        entity.clearItems(); // Prepare for fresh item list
        // FIX: Use cart.getItems()
        for (CartItem item : cart.getItems()) {
            Variant variant = item.getVariant();
            // FIX: Use variant.getId().getId() and item.getQuantity()
            entity.addItem(variant.getId().getId(), item.getQuantity());
        }

        repository.save(entity);
        return cart; // Return the original domain object as per port contract
    }

    @Override
    @Transactional
    public void delete(CartId id) {
        // This was already correct
        repository.findById(id.getId()).ifPresent(repository::delete);
    }

    private ShoppingCart toDomain(ShoppingCartEntity entity) {
        CartItems cartItems = new CartItems(); // Start with empty CartItems

        // Reconstruct CartItems properly
        for (CartItemEntity itemEntity : entity.getItems()) {
             Variant variant = variantPort.findById(new VariantId(itemEntity.getVariantId()))
                    .orElseThrow(() -> new RuntimeException( // Consider a more specific exception
                            "Variant not found during cart hydration: " + itemEntity.getVariantId()
                    ));
            // Let CartItems handle quantity merging and currency checks
            cartItems.add(variant, itemEntity.getQuantity());
        }

        return new ShoppingCart(
                new CartId(entity.getUuid()),
                // *** FIX: Assuming UserId.of(UUID) exists or use appropriate factory ***
                UserId.of(entity.getUserId()),
                cartItems, // Pass the constructed CartItems
                entity.getStatus(),
                Instant.ofEpochMilli(entity.getCreatedAt()), // Convert long to Instant
                Instant.ofEpochMilli(entity.getUpdatedAt()) // Convert long to Instant
        );
    }
}

