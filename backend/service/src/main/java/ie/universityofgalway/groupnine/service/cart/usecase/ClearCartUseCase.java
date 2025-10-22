package ie.universityofgalway.groupnine.service.cart.usecase;

import ie.universityofgalway.groupnine.domain.cart.ShoppingCart;
import ie.universityofgalway.groupnine.service.cart.ShoppingCartPort;

public class ClearCartUseCase {
    private final ShoppingCartPort cartPort;

    public ClearCartUseCase(ShoppingCartPort cartPort) {
        this.cartPort = cartPort;
    }

    public ShoppingCart execute(String cartId) {
        ShoppingCart cart = cartPort.findById(new ie.universityofgalway.groupnine.domain.cart.CartId(cartId))
                .orElseThrow(() -> new IllegalArgumentException("Cart not found: " + cartId));

        cart.clear();
        return cartPort.save(cart);
    }
}

