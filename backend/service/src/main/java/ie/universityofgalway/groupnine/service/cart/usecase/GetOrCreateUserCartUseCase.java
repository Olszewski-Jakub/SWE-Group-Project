package ie.universityofgalway.groupnine.service.cart.usecase;

import ie.universityofgalway.groupnine.domain.cart.ShoppingCart;
import ie.universityofgalway.groupnine.domain.user.UserId;
import ie.universityofgalway.groupnine.service.cart.ShoppingCartPort;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class GetOrCreateUserCartUseCase {
    private final ShoppingCartPort carts;

    public GetOrCreateUserCartUseCase(ShoppingCartPort carts) {
        this.carts = Objects.requireNonNull(carts);
    }

    public ShoppingCart execute(UserId userId) {
        Objects.requireNonNull(userId, "userId");
        return carts.findByUserId(userId)
                .orElseGet(() -> {
                    ShoppingCart newCart = ShoppingCart.createNew(userId);
                    carts.save(newCart);
                    return newCart;
                });
    }
}
