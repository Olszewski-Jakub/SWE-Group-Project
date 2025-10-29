package ie.universityofgalway.groupnine.service.cart.usecase;

import ie.universityofgalway.groupnine.domain.cart.ShoppingCart;
import ie.universityofgalway.groupnine.domain.user.UserId;
import ie.universityofgalway.groupnine.service.cart.port.ShoppingCartPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class GetOrCreateUserCartUseCaseTest {

    ShoppingCartPort cartPort = mock(ShoppingCartPort.class);
    GetOrCreateUserCartUseCase uc;
    UserId userId;

    @BeforeEach
    void setup() {
        uc = new GetOrCreateUserCartUseCase(cartPort);
        userId = UserId.newId();
    }

    @Test
    void returns_existing_cart_if_found() {
        ShoppingCart cart = ShoppingCart.createNew(userId);
        when(cartPort.findByUserId(userId)).thenReturn(Optional.of(cart));

        ShoppingCart result = uc.execute(userId);
        assertEquals(cart, result);
    }

    @Test
    void creates_new_cart_if_none_exists() {
        when(cartPort.findByUserId(userId)).thenReturn(Optional.empty());

        ShoppingCart result = uc.execute(userId);
        assertNotNull(result);
        verify(cartPort).save(result);
    }
}
