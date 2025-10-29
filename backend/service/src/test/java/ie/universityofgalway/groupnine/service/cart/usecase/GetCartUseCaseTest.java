package ie.universityofgalway.groupnine.service.cart.usecase;

import ie.universityofgalway.groupnine.domain.cart.CartId;
import ie.universityofgalway.groupnine.domain.cart.ShoppingCart;
import ie.universityofgalway.groupnine.domain.user.UserId;
import ie.universityofgalway.groupnine.domain.cart.exception.CartNotFoundException;
import ie.universityofgalway.groupnine.service.cart.port.ShoppingCartPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class GetCartUseCaseTest {

    private ShoppingCartPort cartPort;
    private GetCartUseCase uc;
    private ShoppingCart cart;
    private CartId cartId;

    @BeforeEach
    void setup() {
        cartPort = mock(ShoppingCartPort.class);
        uc = new GetCartUseCase(cartPort);

        cartId = new CartId(UUID.randomUUID());
        cart = ShoppingCart.createNew(UserId.newId());

        when(cartPort.findById(cartId)).thenReturn(Optional.of(cart));
    }

    @Test
    void retrieves_cart_successfully() {
        uc.execute(cartId);
        verify(cartPort).findById(cartId);
    }

    @Test
    void throws_if_cart_not_found() {
        when(cartPort.findById(cartId)).thenReturn(Optional.empty());
        assertThrows(CartNotFoundException.class, () -> uc.execute(cartId));
    }
}