package ie.universityofgalway.groupnine.infrastructure.cart.jpa;

import ie.universityofgalway.groupnine.domain.cart.CartStatus;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * JPA entity representing a persisted shopping cart aggregate.
 * <p>
 * Each {@code ShoppingCartEntity} stores ownership information, status,
 * timestamps, and a collection of {@link CartItemEntity} items.
 * <p>
 * The relationship between cart and items is one-to-many with
 * cascade and orphan removal enabled.
 */
@Entity
@Table(name = "shopping_carts")
public class ShoppingCartEntity {

    /** Primary identifier for the shopping cart. */
    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID uuid;

    /** Identifier of the user owning this cart. */
    @Column(nullable = false, columnDefinition = "BINARY(16)")
    private UUID userId;

    /** Lifecycle status of the shopping cart (ACTIVE, CHECKED_OUT, ABANDONED). */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "cart_status")
    private CartStatus status = CartStatus.ACTIVE;

    /** Set of items associated with this shopping cart. */
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<CartItemEntity> items = new HashSet<>();

    /** Creation timestamp (epoch milliseconds). */
    @Column(nullable = false)
    private long createdAt;

    /** Last update timestamp (epoch milliseconds). */
    @Column(nullable = false)
    private long updatedAt;

    /** Default constructor for JPA. Initializes UUID and timestamps. */
    public ShoppingCartEntity() {
        this.uuid = UUID.randomUUID();
        long now = System.currentTimeMillis();
        this.createdAt = now;
        this.updatedAt = now;
    }

    /**
     * Constructs a new shopping cart entity.
     *
     * @param uuid   unique cart identifier
     * @param userId owning user's identifier
     */
    public ShoppingCartEntity(UUID uuid, UUID userId) {
        this.uuid = uuid;
        this.userId = userId;
        long now = System.currentTimeMillis();
        this.createdAt = now;
        this.updatedAt = now;
    }

    /** @return cart UUID */
    public UUID getUuid() { return uuid; }

    /** @return user UUID */
    public UUID getUserId() { return userId; }

    /** Sets the user ID. */
    public void setUserId(UUID userId) { this.userId = userId; }

    /** @return current cart status */
    public CartStatus getStatus() { return status; }

    /** Sets the cart lifecycle status. */
    public void setStatus(CartStatus status) { this.status = status; }

    /** @return items in this cart */
    public Set<CartItemEntity> getItems() { return items; }

    /**
     * Replaces the entire set of cart items.
     * Automatically sets the cart reference on each child entity.
     *
     * @param items the new set of cart items
     */
    public void setItems(Set<CartItemEntity> items) {
        this.items.clear();
        if (items != null) {
            for (CartItemEntity item : items) {
                item.setCart(this);
            }
            this.items.addAll(items);
        }
    }

    /** @return creation timestamp (epoch ms) */
    public long getCreatedAt() { return createdAt; }

    /** @return last updated timestamp (epoch ms) */
    public long getUpdatedAt() { return updatedAt; }

    /** Updates the modification timestamp. */
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    /** Removes all items from the cart. */
    public void clearItems() {
        items.clear();
    }

    /**
     * Adds a new item to the cart.
     *
     * @param variantId product variant ID
     * @param quantity  item quantity
     */
    public void addItem(UUID variantId, int quantity) {
        CartItemEntity item = new CartItemEntity();
        item.setCart(this);
        item.setVariantId(variantId);
        item.setQuantity(quantity);
        items.add(item);
    }
}
