package ie.universityofgalway.groupnine.service.cart.usecase;

import ie.universityofgalway.groupnine.domain.cart.ShoppingCart;
import ie.universityofgalway.groupnine.domain.product.Money;
import ie.universityofgalway.groupnine.domain.product.Stock;
import ie.universityofgalway.groupnine.domain.product.Variant;
import ie.universityofgalway.groupnine.domain.product.VariantId;
import ie.universityofgalway.groupnine.domain.user.UserId;
import ie.universityofgalway.groupnine.service.cart.ShoppingCartPort;
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

    ShoppingCartPort cartPort = mock(ShoppingCartPort.class);
    VariantPort variantPort = mock(VariantPort.class);


    UpdateCartItemUseCase uc;
    ShoppingCart cart;
    Variant variant;
    String cartId;
    UUID variantId;

    @BeforeEach
    void setup() {
        RemoveCartItemUseCase removeUseCase = new RemoveCartItemUseCase(cartPort, variantPort);
        uc = new UpdateCartItemUseCase(cartPort, variantPort, removeUseCase);

        cartId = UUID.randomUUID().toString();
        variantId = UUID.randomUUID();
        cart = ShoppingCart.createNew(UserId.newId());
        variant = mock(Variant.class);

        // Mock stock
        Stock stock = new Stock(10, 0);
        when(variant.stock()).thenReturn(stock);

        // Mock price
        Money price = new Money(new BigDecimal("10.00"), Currency.getInstance("EUR"));
        when(variant.price()).thenReturn(price);

        when(cartPort.findById(any())).thenReturn(Optional.of(cart));
        when(variantPort.findById(new VariantId(variantId))).thenReturn(Optional.of(variant));
        when(cartPort.save(cart)).thenReturn(cart);
    }




    @Test
    void updates_item_quantity_successfully() {
        uc.execute(cartId, variantId, 5);
        verify(cartPort).save(cart);
    }

    @Test
    void throws_if_cart_not_found() {
        when(cartPort.findById(any())).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> uc.execute(cartId, variantId, 1));
    }

    @Test
    void throws_if_variant_not_found() {
        when(variantPort.findById(new VariantId(variantId))).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> uc.execute(cartId, variantId, 1));
    }

    @Test
    void decreases_item_quantity_successfully() {
        // assume current quantity > 5 in the cart
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

        // Use hasItem instead of contains
        assertFalse(cart.items().hasItem(variant));
    }
}

