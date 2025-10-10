package ie.universityofgalway.groupnine.domain.cart;

import ie.universityofgalway.groupnine.domain.product.Money;
import ie.universityofgalway.groupnine.domain.product.Variant;
import java.util.Objects;

/**
 * A single line item inside a {@link ShoppingCart}.
 * <p>
 * Each cart item represents one product variant and a quantity.
 * The object is immutable and validated upon creation.
 */
public record CartItem(Variant variant, int quantity) {

    /**
     * Validates and constructs a new {@code CartItem}.
     *
     * @param variant the product variant being purchased (non-null)
     * @param quantity number of units (must be > 0)
     * @throws NullPointerException if {@code variant} or its price is null
     * @throws IllegalArgumentException if {@code quantity} ≤ 0
     */
    public CartItem {
        Objects.requireNonNull(variant, "variant cannot be null");
        Objects.requireNonNull(variant.price(), "variant price cannot be null");
        if (quantity <= 0) throw new IllegalArgumentException("quantity must be > 0");
    }

    /**
     * Computes the subtotal (unit price × quantity).
     *
     * @return total price for this line item
     */
    public Money subtotal() {
        return variant.price().multiply(quantity);
    }

    /**
     * Returns a new {@code CartItem} with increased quantity.
     *
     * @param extra number of units to add (must be > 0)
     * @return new cart item with updated quantity
     * @throws IllegalArgumentException if {@code extra} ≤ 0
     */
    public CartItem increaseQuantity(int extra) {
        if (extra <= 0) throw new IllegalArgumentException("extra must be > 0");
        return new CartItem(variant, Math.addExact(quantity, extra));
    }

    /**
     * Returns a new {@code CartItem} with decreased quantity.
     *
     * @param amount number of units to remove (must be > 0 and ≤ current quantity)
     * @return new cart item with updated quantity
     * @throws IllegalArgumentException if amount is invalid
     */
    public CartItem decreaseQuantity(int amount) {
        if (amount <= 0 || amount > quantity)
            throw new IllegalArgumentException("invalid decrease amount");
        return new CartItem(variant, quantity - amount);
    }
}
