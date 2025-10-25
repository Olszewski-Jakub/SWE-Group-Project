package ie.universityofgalway.groupnine.service.cart.usecase;

import ie.universityofgalway.groupnine.domain.cart.CartId;
import ie.universityofgalway.groupnine.domain.cart.ShoppingCart;
import ie.universityofgalway.groupnine.service.cart.ShoppingCartPort;

import java.util.Objects;

/**
 * Retrieves a shopping cart by its ID.
 */
public class GetCartUseCase {

    private final ShoppingCartPort cartPort;

    public GetCartUseCase(ShoppingCartPort cartPort) {
        this.cartPort = Objects.requireNonNull(cartPort);
    }

    /**
     * Executes the use case to retrieve a shopping cart by its ID.
     *
     * @param cartIdRaw the UUID of the cart as a string
     * @return the ShoppingCart domain object
     * @throws IllegalArgumentException if the cart does not exist
     */
    public ShoppingCart execute(String cartIdRaw) {
        Objects.requireNonNull(cartIdRaw, "cartId");

        CartId cartId = CartId.of(java.util.UUID.fromString(cartIdRaw));

        return cartPort.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found for ID: " + cartIdRaw));
    }
}
