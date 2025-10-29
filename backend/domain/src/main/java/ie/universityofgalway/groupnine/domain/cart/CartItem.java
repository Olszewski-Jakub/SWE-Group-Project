package ie.universityofgalway.groupnine.domain.cart;

import ie.universityofgalway.groupnine.domain.product.Money;
import ie.universityofgalway.groupnine.domain.product.Variant;
import java.util.Objects;

/**
 * Represents a single, immutable line item within a ShoppingCart, associating a
 * product {@link Variant} with a specific quantity.
 */
public class CartItem {

    private final Variant variant;
    private final int quantity;

    /**
     * Constructs a new CartItem.
     *
     * @param variant The product variant for this line item. Must not be null.
     * @param quantity The number of units of the variant. Must be a positive integer.
     * @throws NullPointerException if the variant or its price is null.
     * @throws IllegalArgumentException if the quantity is not greater than zero.
     */
    public CartItem(Variant variant, int quantity) {
        this.variant = Objects.requireNonNull(variant, "variant cannot be null");
        Objects.requireNonNull(variant.getPrice(), "variant price cannot be null");
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be > 0");
        }
        this.quantity = quantity;
    }

    /**
     * @return The {@link Variant} associated with this cart item.
     */
    public Variant getVariant() {
        return variant;
    }

    /**
     * @return The quantity of the variant in this cart item.
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Calculates the subtotal for this line item by multiplying the variant's price by the quantity.
     *
     * @return A {@link Money} object representing the total value of this item.
     */
    public Money subtotal() {
        return variant.getPrice().multiply(quantity);
    }

    /**
     * Creates a new CartItem instance with an increased quantity.
     *
     * @param extra The positive amount to add to the current quantity.
     * @return A new {@link CartItem} with the updated quantity.
     * @throws IllegalArgumentException if extra is not a positive integer or if the operation results in an integer overflow.
     */
    public CartItem increaseQuantity(int extra) {
        if (extra <= 0) {
            throw new IllegalArgumentException("extra must be > 0");
        }
        try {
            return new CartItem(variant, Math.addExact(quantity, extra));
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("Quantity would overflow", e);
        }
    }

    /**
     * Creates a new CartItem instance with a decreased quantity.
     *
     * @param amount The positive amount to subtract from the current quantity.
     * @return A new {@link CartItem} with the updated quantity.
     * @throws IllegalArgumentException if the amount is invalid (e.g., negative or greater than the current quantity).
     */
    public CartItem decreaseQuantity(int amount) {
        if (amount <= 0 || amount > quantity) {
            throw new IllegalArgumentException("invalid decrease amount: must be > 0 and <= current quantity");
        }
        return new CartItem(variant, quantity - amount);
    }
}