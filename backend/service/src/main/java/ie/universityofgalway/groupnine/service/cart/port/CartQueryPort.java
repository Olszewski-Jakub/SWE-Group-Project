package ie.universityofgalway.groupnine.service.cart.port;

import ie.universityofgalway.groupnine.domain.cart.CartId;
import ie.universityofgalway.groupnine.domain.cart.ShoppingCart;
import ie.universityofgalway.groupnine.domain.user.UserId;

import java.util.Optional;

/**
 * Query port focused on retrieving the active cart for a given owner.
 * Implementations should ensure the cart is ACTIVE and owned by the given user.
 */
public interface CartQueryPort {
    Optional<ShoppingCart> loadActiveCartByIdAndOwner(CartId cartId, UserId ownerId);
}

