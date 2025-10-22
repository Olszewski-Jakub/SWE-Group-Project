package ie.universityofgalway.groupnine.service.cart.usecase;

import ie.universityofgalway.groupnine.domain.cart.CartId;
import ie.universityofgalway.groupnine.domain.cart.ShoppingCart;
import ie.universityofgalway.groupnine.domain.user.UserId;
import ie.universityofgalway.groupnine.service.cart.CartNotFoundException;
import ie.universityofgalway.groupnine.service.cart.ShoppingCartPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ClearCartUseCaseTest {

    private ShoppingCartPort cartPort;
    private ClearCartUseCase uc;
    private ShoppingCart cart;
    private CartId cartId;

    @BeforeEach
    void setup() {
        cartPort = mock(ShoppingCartPort.class);
        uc = new ClearCartUseCase(cartPort);
        cartId = new CartId(UUID.randomUUID());
        cart = ShoppingCart.createNew(UserId.newId());

        when(cartPort.findById(cartId)).thenReturn(Optional.of(cart));
        when(cartPort.save(cart)).thenReturn(cart);
    }

    @Test
    void clears_cart_successfully() {
        uc.execute(cartId);
        verify(cartPort).save(cart);
    }

    @Test
    void throws_if_cart_not_found() {
        when(cartPort.findById(cartId)).thenReturn(Optional.empty());
        assertThrows(CartNotFoundException.class, () -> uc.execute(cartId));
    }
}

