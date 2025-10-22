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
        Objects.requireNonNull(variant.price(), "variant price cannot be null");
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
        return variant.price().multiply(quantity);
    }

    public CartItem increaseQuantity(int extra) {
        if (extra <= 0) throw new IllegalArgumentException("extra must be > 0");
        return new CartItem(variant, Math.addExact(quantity, extra));
    }

    public CartItem decreaseQuantity(int amount) {
        if (amount <= 0 || amount > quantity)
            throw new IllegalArgumentException("invalid decrease amount");
        return new CartItem(variant, quantity - amount);
    }
}
