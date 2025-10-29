package ie.universityofgalway.groupnine.service.cart.usecase;

import ie.universityofgalway.groupnine.domain.cart.CartId;
import ie.universityofgalway.groupnine.domain.cart.exception.CartNotFoundException;
import ie.universityofgalway.groupnine.domain.cart.exception.InsufficientStockException;
import ie.universityofgalway.groupnine.domain.cart.ShoppingCart;
import ie.universityofgalway.groupnine.domain.product.*;
import ie.universityofgalway.groupnine.domain.product.exception.VariantNotFoundException;
import ie.universityofgalway.groupnine.domain.user.UserId;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;
import java.util.UUID;

import ie.universityofgalway.groupnine.service.cart.port.ShoppingCartPort;
import ie.universityofgalway.groupnine.service.product.port.VariantPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the {@link AddCartItemUseCase}.
 */
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

    /**
     * Sets up the test environment before each test case, initializing mocks
     * and preparing test data such as users, carts, and product variants.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        cartId = new CartId(UUID.randomUUID());
        variantId = new VariantId(UUID.randomUUID());
        userId = UserId.of(UUID.randomUUID());

        cart = ShoppingCart.createNew(userId);
        variant = mock(Variant.class);

        when(variant.getStock()).thenReturn(new Stock(10, 0));
        when(variant.getPrice()).thenReturn(new Money(BigDecimal.valueOf(100), Currency.getInstance("EUR")));
        when(variant.getId()).thenReturn(variantId);

        when(carts.findById(cartId)).thenReturn(Optional.of(cart));
        when(variants.findById(variantId)).thenReturn(Optional.of(variant));
        when(carts.save(any(ShoppingCart.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    /**
     * Verifies that the use case successfully adds an item to the cart
     * when provided with valid input.
     */
    @Test
    void execute_shouldAddItemToCart_whenValid() {
        int quantity = 2;
        ShoppingCart updatedCart = addCartItemUseCase.execute(cartId, variantId, quantity);

        assertNotNull(updatedCart);
        assertEquals(1, updatedCart.getItems().size());
        assertEquals(quantity, updatedCart.getItems().get(0).getQuantity());
        assertEquals(variantId, updatedCart.getItems().get(0).getVariant().getId());

        verify(carts).findById(cartId);
        verify(variants).findById(variantId);
        verify(carts).save(cart);
    }

    /**
     * Verifies that the use case throws an {@link IllegalArgumentException}
     * when the quantity is zero.
     */
    @Test
    void execute_shouldThrowException_whenQuantityIsZero() {
        assertThrows(IllegalArgumentException.class, () -> {
            addCartItemUseCase.execute(cartId, variantId, 0);
        });
    }

    /**
     * Verifies that the use case throws an {@link IllegalArgumentException}
     * when the quantity is negative.
     */
    @Test
    void execute_shouldThrowException_whenQuantityIsNegative() {
        assertThrows(IllegalArgumentException.class, () -> {
            addCartItemUseCase.execute(cartId, variantId, -1);
        });
    }

    /**
     * Verifies that the use case throws a {@link CartNotFoundException}
     * when the specified cart does not exist.
     */
    @Test
    void execute_shouldThrowException_whenCartNotFound() {
        when(carts.findById(cartId)).thenReturn(Optional.empty());
        assertThrows(CartNotFoundException.class, () -> {
            addCartItemUseCase.execute(cartId, variantId, 1);
        });
    }

    /**
     * Verifies that the use case throws a {@link VariantNotFoundException}
     * when the specified product variant does not exist.
     */
    @Test
    void execute_shouldThrowException_whenVariantNotFound() {
        when(variants.findById(variantId)).thenReturn(Optional.empty());
        assertThrows(VariantNotFoundException.class, () -> {
            addCartItemUseCase.execute(cartId, variantId, 1);
        });
    }

    /**
     * Verifies that the use case throws an {@link InsufficientStockException}
     * when the requested quantity exceeds the available stock.
     */
    @Test
    void execute_shouldThrowException_whenInsufficientStock() {
        int quantity = 11;
        assertThrows(InsufficientStockException.class, () -> {
            addCartItemUseCase.execute(cartId, variantId, quantity);
        });
        verify(carts, never()).save(any(ShoppingCart.class));
    }
}