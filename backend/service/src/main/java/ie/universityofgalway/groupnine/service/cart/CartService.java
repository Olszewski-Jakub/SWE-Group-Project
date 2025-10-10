package ie.universityofgalway.groupnine.service.cart;

import ie.universityofgalway.groupnine.domain.cart.CartId;
import ie.universityofgalway.groupnine.domain.cart.ShoppingCart;
import ie.universityofgalway.groupnine.domain.product.Variant;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Application service for managing shopping carts.
 *
 * Coordinates domain logic with persistence via CartPort.
 */
@Service
public class CartService {

    private final ShoppingCartPort cartPort;

    public CartService(ShoppingCartPort cartPort) {
        this.cartPort = cartPort;
    }

    public ShoppingCart getCart(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Cart ID must not be blank");
        }

        UUID uuid;
        try {
            uuid = UUID.fromString(id.trim());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid UUID format: " + id);
        }

        return cartPort.findById(new CartId(uuid))
                .orElseThrow(() -> new CartNotFoundException(id));
    }

    public ShoppingCart addItem(String cartId, Variant variant, int quantity) {
        if (variant == null) throw new IllegalArgumentException("Variant must not be null");
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");

        ShoppingCart cart = getCart(cartId);
        cart.addItem(variant, quantity);
        cartPort.save(cart);
        return cart;
    }

    public ShoppingCart removeItem(String cartId, Variant variant) {
        if (variant == null) throw new IllegalArgumentException("Variant must not be null");

        ShoppingCart cart = getCart(cartId);
        cart.removeItem(variant);
        cartPort.save(cart);
        return cart;
    }
}