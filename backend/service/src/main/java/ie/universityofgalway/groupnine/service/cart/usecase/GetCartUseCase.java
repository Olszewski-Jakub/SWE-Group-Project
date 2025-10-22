package ie.universityofgalway.groupnine.service.cart.usecase;

import ie.universityofgalway.groupnine.domain.cart.CartId;
import ie.universityofgalway.groupnine.domain.cart.ShoppingCart;
import ie.universityofgalway.groupnine.service.cart.CartNotFoundException;
import ie.universityofgalway.groupnine.service.cart.ShoppingCartPort;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Use case for retrieving a shopping cart by its ID.
 */
@Service
public class GetCartUseCase {

    private final ShoppingCartPort cartPort;

    public GetCartUseCase(ShoppingCartPort cartPort) {
        this.cartPort = Objects.requireNonNull(cartPort);
    }

    /**
     * Executes the use case to retrieve a shopping cart.
     *
     * @param cartId the {@link CartId} of the cart
     * @return the {@link ShoppingCart} if found
     * @throws CartNotFoundException if the cart does not exist
     */
    public ShoppingCart execute(CartId cartId) {
        return cartPort.findById(cartId)
                .orElseThrow(() -> new CartNotFoundException(cartId.toString()));
    }
}
