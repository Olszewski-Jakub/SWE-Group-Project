package ie.universityofgalway.groupnine.service.cart.usecase;

import ie.universityofgalway.groupnine.domain.cart.CartId;
import ie.universityofgalway.groupnine.domain.cart.ShoppingCart;
import ie.universityofgalway.groupnine.domain.product.Variant;
import ie.universityofgalway.groupnine.domain.product.VariantId;
import ie.universityofgalway.groupnine.domain.user.UserId;
import ie.universityofgalway.groupnine.service.cart.ShoppingCartPort;
import ie.universityofgalway.groupnine.service.product.VariantPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class RemoveCartItemUseCaseTest {

    ShoppingCartPort cartPort = mock(ShoppingCartPort.class);
    VariantPort variantPort = mock(VariantPort.class);

    RemoveCartItemUseCase uc;
    ShoppingCart cart;
    Variant variant;
    UUID cartId;
    UUID variantId;

    @BeforeEach
    void setup() {
        uc = new RemoveCartItemUseCase(cartPort, variantPort);
        cartId = UUID.randomUUID();
        variantId = UUID.randomUUID();

        cart = ShoppingCart.createNew(UserId.newId());

        variant = mock(Variant.class);

        when(cartPort.findById(new CartId(cartId))).thenReturn(Optional.of(cart));
        when(variantPort.findById(new VariantId(variantId))).thenReturn(Optional.of(variant));
        when(cartPort.save(cart)).thenReturn(cart);
    }


    @Test
    void removes_item_successfully() {
        uc.execute(cartId.toString(), variantId);
        verify(cartPort).save(cart);
    }

    @Test
    void throws_if_cart_not_found() {
        when(cartPort.findById(new CartId(cartId))).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> uc.execute(cartId.toString(), variantId));
    }

    @Test
    void throws_if_variant_not_found() {
        when(variantPort.findById(new VariantId(variantId))).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> uc.execute(cartId.toString(), variantId));
    }
}
