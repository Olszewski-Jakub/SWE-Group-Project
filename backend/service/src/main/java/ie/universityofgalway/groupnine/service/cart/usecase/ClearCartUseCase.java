package ie.universityofgalway.groupnine.service.cart.usecase;

import ie.universityofgalway.groupnine.domain.cart.CartId;
import ie.universityofgalway.groupnine.domain.cart.ShoppingCart;
import ie.universityofgalway.groupnine.domain.cart.exception.CartNotFoundException;
import ie.universityofgalway.groupnine.service.cart.port.ShoppingCartPort;
import org.springframework.stereotype.Service;

/**
 * Use case for clearing all items from a shopping cart.
 */
@Service
public class ClearCartUseCase {

    private final ShoppingCartPort cartPort;

    public ClearCartUseCase(ShoppingCartPort cartPort) {
        this.cartPort = cartPort;
    }

    /**
     * Clears all items from the given cart.
     *
     * @param cartId the {@link CartId} of the cart
     * @return the updated {@link ShoppingCart} with no items
     * @throws CartNotFoundException if the cart does not exist
     */
    public ShoppingCart execute(CartId cartId) {
        ShoppingCart cart = cartPort.findById(cartId)
                .orElseThrow(() -> new CartNotFoundException(cartId.toString()));

        cart.clear();
        return cartPort.save(cart);
    }
}
