package ie.universityofgalway.groupnine.service.cart;

import ie.universityofgalway.groupnine.domain.cart.CartId;
import ie.universityofgalway.groupnine.domain.cart.ShoppingCart;
import ie.universityofgalway.groupnine.domain.user.UserId;

import java.util.Optional;

public interface ShoppingCartPort {
    Optional<ShoppingCart> findById(CartId id);

    Optional<ShoppingCart> findByUserId(UserId userId);
    ShoppingCart save(ShoppingCart cart);

    void delete(CartId id);
}
