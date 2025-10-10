package ie.universityofgalway.groupnine.service.cart;

import ie.universityofgalway.groupnine.domain.cart.CartId;
import ie.universityofgalway.groupnine.domain.cart.ShoppingCart;

import java.util.Optional;

public interface ShoppingCartPort {
    Optional<ShoppingCart> findById(CartId id);

    ShoppingCart save(ShoppingCart cart);

    void delete(CartId id);
}
