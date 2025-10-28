package ie.universityofgalway.groupnine.domain.cart;

import ie.universityofgalway.groupnine.domain.product.Money;
import ie.universityofgalway.groupnine.domain.product.Variant;

import java.util.Objects;

/**
 * A single line item inside a ShoppingCart.
 */
public class CartItem {

    private final Variant variant;
    private final int quantity;

    public CartItem(Variant variant, int quantity) {
        this.variant = Objects.requireNonNull(variant, "variant cannot be null");
        // FIX: Changed variant.price() to variant.getPrice()
        Objects.requireNonNull(variant.getPrice(), "variant price cannot be null");
        if (quantity <= 0) throw new IllegalArgumentException("quantity must be > 0");
        this.quantity = quantity;
    }

    public Variant getVariant() {
        return variant;
    }

    public int getQuantity() {
        return quantity;
    }

    public Money subtotal() {
        // FIX: Changed variant.price() to variant.getPrice()
        return variant.getPrice().multiply(quantity);
    }

    public CartItem increaseQuantity(int extra) {
        if (extra <= 0) throw new IllegalArgumentException("extra must be > 0");
        // Use Math.addExact to prevent integer overflow
        try {
            return new CartItem(variant, Math.addExact(quantity, extra));
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("Quantity would overflow", e);
        }
    }

    public CartItem decreaseQuantity(int amount) {
        if (amount <= 0 || amount > quantity)
            throw new IllegalArgumentException("invalid decrease amount: must be > 0 and <= current quantity");
        return new CartItem(variant, quantity - amount);
    }
}
