package ie.universityofgalway.groupnine.service.cart.usecase;

import ie.universityofgalway.groupnine.domain.cart.CartId;
import ie.universityofgalway.groupnine.domain.cart.ShoppingCart;
import ie.universityofgalway.groupnine.domain.user.UserId;
import ie.universityofgalway.groupnine.service.cart.ShoppingCartPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ClearCartUseCaseTest {

    ShoppingCartPort cartPort = mock(ShoppingCartPort.class);
    ClearCartUseCase uc;
    ShoppingCart cart;
    UUID cartId;

    @BeforeEach
    void setup() {
        uc = new ClearCartUseCase(cartPort);
        cartId = UUID.randomUUID();
        cart = ShoppingCart.createNew(UserId.newId()); // <-- use a proper UserId
        when(cartPort.findById(new CartId(cartId))).thenReturn(Optional.of(cart));
        when(cartPort.save(cart)).thenReturn(cart);
    }

    @Test
    void clears_cart_successfully() {
        uc.execute(cartId.toString());
        verify(cartPort).save(cart);
    }

    @Test
    void throws_if_cart_not_found() {
        when(cartPort.findById(new CartId(cartId))).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> uc.execute(cartId.toString()));
    }
}
