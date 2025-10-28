package ie.universityofgalway.groupnine.service.cart.usecase;

import ie.universityofgalway.groupnine.domain.cart.CartId;
// Removed unused CartStatus import
import ie.universityofgalway.groupnine.domain.cart.ShoppingCart;
import ie.universityofgalway.groupnine.domain.product.*;
import ie.universityofgalway.groupnine.domain.user.UserId;
import ie.universityofgalway.groupnine.service.cart.CartNotFoundException;
import ie.universityofgalway.groupnine.service.cart.ShoppingCartPort;
import ie.universityofgalway.groupnine.service.product.VariantNotFoundException;
import ie.universityofgalway.groupnine.service.product.VariantPort;
// Added missing InsufficientStockException import
import ie.universityofgalway.groupnine.domain.cart.InsufficientStockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
// Removed unused List import
import java.util.Optional;
import java.util.UUID;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Renamed class to match file name convention
class AddCartItemUseCaseTest {

    @Mock
    private ShoppingCartPort carts;

    @Mock
    private VariantPort variants;

    @InjectMocks
    private AddCartItemUseCase addCartItemUseCase;

    private CartId cartId;
    private VariantId variantId;
    private ShoppingCart cart;
    private Variant variant;
    private UserId userId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        cartId = new CartId(UUID.randomUUID());
        variantId = new VariantId(UUID.randomUUID());
        userId = UserId.of(UUID.randomUUID());
        // Removed unused 'now' variable

        cart = ShoppingCart.createNew(userId); // Use factory method

        variant = mock(Variant.class); // Mock the Variant class

        // *** FIX: Use getters in Mockito setups ***
        when(variant.getStock()).thenReturn(new Stock(10, 0));
        when(variant.getPrice()).thenReturn(new Money(BigDecimal.valueOf(100), Currency.getInstance("EUR")));
        when(variant.getId()).thenReturn(variantId); // Ensure the mock returns its ID

        when(carts.findById(cartId)).thenReturn(Optional.of(cart));
        when(variants.findById(variantId)).thenReturn(Optional.of(variant));
        when(carts.save(any(ShoppingCart.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void execute_shouldAddItemToCart_whenValid() {
        int quantity = 2;
        ShoppingCart updatedCart = addCartItemUseCase.execute(cartId, variantId, quantity);

        assertNotNull(updatedCart);
        // *** FIX: Use getter getItems() ***
        assertEquals(1, updatedCart.getItems().size());
        assertEquals(quantity, updatedCart.getItems().get(0).getQuantity());
        // *** FIX: Use getter getId() ***
        assertEquals(variantId, updatedCart.getItems().get(0).getVariant().getId());

        verify(carts).findById(cartId);
        verify(variants).findById(variantId);
        verify(carts).save(cart); // Verify save was called
    }

    @Test
    void execute_shouldThrowException_whenQuantityIsZero() {
        assertThrows(IllegalArgumentException.class, () -> {
            addCartItemUseCase.execute(cartId, variantId, 0);
        });
    }
     @Test
    void execute_shouldThrowException_whenQuantityIsNegative() {
        assertThrows(IllegalArgumentException.class, () -> {
            addCartItemUseCase.execute(cartId, variantId, -1);
        });
    }


    @Test
    void execute_shouldThrowException_whenCartNotFound() {
        when(carts.findById(cartId)).thenReturn(Optional.empty());
        assertThrows(CartNotFoundException.class, () -> {
            addCartItemUseCase.execute(cartId, variantId, 1);
        });
    }

    @Test
    void execute_shouldThrowException_whenVariantNotFound() {
        when(variants.findById(variantId)).thenReturn(Optional.empty());
        assertThrows(VariantNotFoundException.class, () -> {
            addCartItemUseCase.execute(cartId, variantId, 1);
        });
    }

    @Test
    void execute_shouldThrowException_whenInsufficientStock() {
        int quantity = 11; // More than available stock (10)
        assertThrows(InsufficientStockException.class, () -> {
            addCartItemUseCase.execute(cartId, variantId, quantity);
        });
        // Verify save was NOT called
        verify(carts, never()).save(any(ShoppingCart.class));
    }
}

