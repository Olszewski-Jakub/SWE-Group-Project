package ie.universityofgalway.groupnine.service.cart.usecase;

import ie.universityofgalway.groupnine.domain.cart.CartId;
import ie.universityofgalway.groupnine.domain.cart.ShoppingCart;
import ie.universityofgalway.groupnine.domain.product.Money;
import ie.universityofgalway.groupnine.domain.product.Stock;
import ie.universityofgalway.groupnine.domain.product.Variant;
import ie.universityofgalway.groupnine.domain.product.VariantId;
import ie.universityofgalway.groupnine.domain.user.UserId;
import ie.universityofgalway.groupnine.service.cart.CartNotFoundException;
import ie.universityofgalway.groupnine.service.cart.ShoppingCartPort;
import ie.universityofgalway.groupnine.service.product.VariantNotFoundException;
import ie.universityofgalway.groupnine.service.product.VariantPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class AddCartItemUseCaseTest {

    private ShoppingCartPort cartPort;
    private VariantPort variantPort;
    private AddCartItemUseCase uc;
    private ShoppingCart cart;
    private Variant variant;
    private CartId cartId;
    private VariantId variantId;

    @BeforeEach
    void setup() {
        cartPort = mock(ShoppingCartPort.class);
        variantPort = mock(VariantPort.class);

        uc = new AddCartItemUseCase(cartPort, variantPort);

        cartId = new CartId(UUID.randomUUID());
        variantId = new VariantId(UUID.randomUUID());

        cart = ShoppingCart.createNew(UserId.newId());

        variant = mock(Variant.class);
        when(variant.stock()).thenReturn(new Stock(10, 0));
        when(variant.price()).thenReturn(new Money(BigDecimal.valueOf(100), Currency.getInstance("EUR")));

        when(cartPort.findById(cartId)).thenReturn(Optional.of(cart));
        when(variantPort.findById(variantId)).thenReturn(Optional.of(variant));
        when(cartPort.save(cart)).thenReturn(cart);
    }

    @Test
    void adds_item_successfully() {
        uc.execute(cartId, variantId, 2);
        verify(cartPort).save(cart);
    }

    @Test
    void throws_if_cart_not_found() {
        when(cartPort.findById(cartId)).thenReturn(Optional.empty());
        assertThrows(CartNotFoundException.class, () -> uc.execute(cartId, variantId, 1));
    }

    @Test
    void throws_if_variant_not_found() {
        when(variantPort.findById(variantId)).thenReturn(Optional.empty());
        assertThrows(VariantNotFoundException.class, () -> uc.execute(cartId, variantId, 1));
    }

    @Test
    void throws_if_quantity_invalid() {
        assertThrows(IllegalArgumentException.class, () -> uc.execute(cartId, variantId, 0));
    }
}
