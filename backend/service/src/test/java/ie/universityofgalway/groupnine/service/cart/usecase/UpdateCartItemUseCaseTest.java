package ie.universityofgalway.groupnine.service.cart.usecase;

import ie.universityofgalway.groupnine.domain.cart.*;
import ie.universityofgalway.groupnine.domain.product.*;
import ie.universityofgalway.groupnine.domain.user.UserId;
import ie.universityofgalway.groupnine.service.cart.CartNotFoundException;
import ie.universityofgalway.groupnine.service.cart.ShoppingCartPort;
import ie.universityofgalway.groupnine.service.product.VariantNotFoundException;
import ie.universityofgalway.groupnine.service.product.VariantPort;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the {@link UpdateCartItemUseCase}.
 */
class UpdateCartItemUseCaseTest {

    @Mock
    private ShoppingCartPort cartPort;

    @Mock
    private VariantPort variantPort;

    @Mock
    private RemoveCartItemUseCase removeCartItemUseCase;

    @InjectMocks
    private UpdateCartItemUseCase updateCartItemUseCase;

    private CartId cartId;
    private VariantId variantId;
    private ShoppingCart cart;
    private Variant variant;
    private UserId userId;

    /**
     * Sets up the test environment before each test case, initializing mocks and
     * preparing a test cart pre-populated with an item.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        cartId = new CartId(UUID.randomUUID());
        variantId = new VariantId(UUID.randomUUID());
        userId = UserId.of(UUID.randomUUID());

        variant = new Variant(
                variantId,
                new Sku("TEST-SKU"),
                new Money(BigDecimal.TEN, Currency.getInstance("EUR")),
                new Stock(10, 0),
                List.of()
        );

        CartItem initialItem = new CartItem(variant, 2);
        cart = new ShoppingCart(cartId, userId, new CartItems(List.of(initialItem)), CartStatus.ACTIVE, Instant.now(), Instant.now());

        when(cartPort.findById(cartId)).thenReturn(Optional.of(cart));
        when(variantPort.findById(variantId)).thenReturn(Optional.of(variant));
        when(cartPort.save(any(ShoppingCart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(removeCartItemUseCase.execute(cartId, variantId)).thenAnswer(invocation -> {
            cart.updateItemQuantity(variant, 0);
            return cartPort.save(cart);
        });
    }

    /**
     * Verifies that the use case correctly updates an item's quantity
     * when provided with a valid positive number.
     */
    @Test
    void execute_shouldUpdateQuantity_whenQuantityIsPositive() {
        int newQuantity = 5;
        ShoppingCart updatedCart = updateCartItemUseCase.execute(cartId, variantId, newQuantity);

        assertNotNull(updatedCart);
        assertEquals(1, updatedCart.getItems().size());
        assertEquals(newQuantity, updatedCart.getItems().get(0).getQuantity());

        verify(cartPort).findById(cartId);
        verify(variantPort).findById(variantId);
        verify(cartPort).save(cart);
        verify(removeCartItemUseCase, never()).execute(any(), any());
    }

    /**
     * Verifies that the use case delegates to the {@link RemoveCartItemUseCase}
     * when the new quantity is zero.
     */
    @Test
    void execute_shouldRemoveItem_whenQuantityIsZero() {
        int newQuantity = 0;
        ShoppingCart updatedCart = updateCartItemUseCase.execute(cartId, variantId, newQuantity);

        assertNotNull(updatedCart);
        assertTrue(updatedCart.getItems().isEmpty());

        verify(removeCartItemUseCase).execute(cartId, variantId);
        verify(cartPort, never()).findById(cartId);
        verify(variantPort, never()).findById(variantId);
    }

    /**
     * Verifies that the use case throws an {@link IllegalArgumentException}
     * when the new quantity is negative.
     */
    @Test
    void execute_shouldThrowException_whenQuantityIsNegative() {
        assertThrows(IllegalArgumentException.class, () -> {
            updateCartItemUseCase.execute(cartId, variantId, -1);
        });
    }

    /**
     * Verifies that the use case throws a {@link CartNotFoundException}
     * when the specified cart does not exist.
     */
    @Test
    void execute_shouldThrowException_whenCartNotFound() {
        when(cartPort.findById(cartId)).thenReturn(Optional.empty());
        assertThrows(CartNotFoundException.class, () -> {
            updateCartItemUseCase.execute(cartId, variantId, 1);
        });
    }

    /**
     * Verifies that the use case throws a {@link VariantNotFoundException}
     * when the specified product variant does not exist.
     */
    @Test
    void execute_shouldThrowException_whenVariantNotFound() {
        when(variantPort.findById(variantId)).thenReturn(Optional.empty());
        assertThrows(VariantNotFoundException.class, () -> {
            updateCartItemUseCase.execute(cartId, variantId, 1);
        });
        verify(removeCartItemUseCase, never()).execute(any(), any());
    }

    /**
     * Verifies that the use case throws an {@link InsufficientStockException}
     * when attempting to increase an item's quantity beyond the available stock.
     */
    @Test
    void execute_shouldThrowException_whenInsufficientStockForIncrease() {
        int newQuantity = 13;
        assertThrows(InsufficientStockException.class, () -> {
            updateCartItemUseCase.execute(cartId, variantId, newQuantity);
        });
        verify(cartPort, never()).save(any(ShoppingCart.class));
    }

    /**
     * Verifies that the use case allows a decrease in quantity even if the total
     * stock is technically insufficient, as no new stock is being reserved.
     */
    @Test
    void execute_shouldAllowDecreaseQuantity_evenWithInsufficientStock() {
         int newQuantity = 1;
         ShoppingCart updatedCart = updateCartItemUseCase.execute(cartId, variantId, newQuantity);

         assertNotNull(updatedCart);
         assertEquals(1, updatedCart.getItems().size());
         assertEquals(newQuantity, updatedCart.getItems().get(0).getQuantity());
         verify(cartPort).save(cart);
    }
}