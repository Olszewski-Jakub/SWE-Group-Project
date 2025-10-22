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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UpdateCartItemUseCaseTest {

    private ShoppingCartPort cartPort;
    private VariantPort variantPort;
    private UpdateCartItemUseCase uc;
    private ShoppingCart cart;
    private Variant variant;
    private CartId cartId;
    private VariantId variantId;

    @BeforeEach
    void setup() {
        cartPort = mock(ShoppingCartPort.class);
        variantPort = mock(VariantPort.class);

        RemoveCartItemUseCase removeUseCase = new RemoveCartItemUseCase(cartPort, variantPort);
        uc = new UpdateCartItemUseCase(cartPort, variantPort, removeUseCase);

        cartId = new CartId(UUID.randomUUID());
        variantId = new VariantId(UUID.randomUUID());

        cart = ShoppingCart.createNew(UserId.newId());
        variant = mock(Variant.class);

        // Mock stock
        when(variant.stock()).thenReturn(new Stock(10, 0));

        // Mock price
        when(variant.price()).thenReturn(new Money(new BigDecimal("10.00"), Currency.getInstance("EUR")));

        // Mock repository
        when(cartPort.findById(cartId)).thenReturn(Optional.of(cart));
        when(variantPort.findById(variantId)).thenReturn(Optional.of(variant));
        when(cartPort.save(cart)).thenReturn(cart);
    }

    @Test
    void updates_item_quantity_successfully() {
        uc.execute(cartId, variantId, 5);
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
    void decreases_item_quantity_successfully() {
        cart.addItem(variant, 10);
        uc.execute(cartId, variantId, 5);
        verify(cartPort).save(cart);
        assertEquals(5, cart.items().getQuantity(variant));
    }

    @Test
    void removes_item_when_quantity_zero() {
        cart.addItem(variant, 5);
        uc.execute(cartId, variantId, 0);
        verify(cartPort).save(cart);
        assertFalse(cart.items().hasItem(variant));
    }
}