package ie.universityofgalway.groupnine.infrastructure.cart.jpa;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * JPA entity representing a single line item in a persisted {@link ShoppingCartEntity}.
 * <p>
 * Each record corresponds to one product variant and its quantity
 * within a specific shopping cart.
 */
@Entity
@Table(name = "cart_items")
public class CartItemEntity {

    /** Unique identifier for the cart item (UUID). */
    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID uuid;

    /** Reference to the owning {@link ShoppingCartEntity}. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_uuid", nullable = false)
    private ShoppingCartEntity cart;

    /** Identifier of the product variant (foreign key to product/variant table). */
    @Column(nullable = false)
    private UUID variantId;

    /** Quantity of the product variant. */
    @Column(nullable = false)
    private int quantity;

    /** Default constructor required by JPA. Initializes a random UUID. */
    public CartItemEntity() {
        this.uuid = UUID.randomUUID();
    }

    /**
     * Constructs a new cart item with the given variant ID and quantity.
     *
     * @param variantId the associated product variant ID
     * @param quantity  number of units (must be positive)
     */
    public CartItemEntity(UUID variantId, int quantity) {
        this.uuid = UUID.randomUUID();
        this.variantId = variantId;
        this.quantity = quantity;
    }

    /** @return the cart item UUID */
    public UUID getUuid() { return uuid; }

    /** @return the owning {@link ShoppingCartEntity} */
    public ShoppingCartEntity getCart() { return cart; }

    /** Sets the parent shopping cart reference. */
    public void setCart(ShoppingCartEntity cart) { this.cart = cart; }

    /** @return the product variant ID */
    public UUID getVariantId() { return variantId; }

    /** Sets the product variant ID. */
    public void setVariantId(UUID variantId) { this.variantId = variantId; }

    /** @return the quantity of the product variant */
    public int getQuantity() { return quantity; }

    /** Sets the product quantity. */
    public void setQuantity(int quantity) { this.quantity = quantity; }
}

