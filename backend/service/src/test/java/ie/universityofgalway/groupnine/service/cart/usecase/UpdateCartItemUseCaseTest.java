package ie.universityofgalway.groupnine.service.cart.usecase;

import ie.universityofgalway.groupnine.domain.cart.*;
import ie.universityofgalway.groupnine.domain.product.*;
import ie.universityofgalway.groupnine.domain.user.UserId;
import ie.universityofgalway.groupnine.service.cart.CartNotFoundException;
import ie.universityofgalway.groupnine.service.cart.ShoppingCartPort;
import ie.universityofgalway.groupnine.service.product.VariantNotFoundException;
import ie.universityofgalway.groupnine.service.product.VariantPort;
import ie.universityofgalway.groupnine.domain.cart.InsufficientStockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        cartId = new CartId(UUID.randomUUID());
        variantId = new VariantId(UUID.randomUUID());
        userId = UserId.of(UUID.randomUUID());

        // Mock the variant with specific stock
        variant = new Variant(
                variantId,
                new Sku("TEST-SKU"),
                new Money(BigDecimal.TEN, Currency.getInstance("EUR")),
                new Stock(10, 0), // Total 10, Reserved 0 -> Available 10
                List.of()
        );

        // Use a real ShoppingCart object to test its internal logic
        // We use @Spy to partially mock it if needed, but here we just need a real instance.
        cart = spy(ShoppingCart.createNew(userId));
        // Manually set the cart ID to our test ID
        // This requires a bit of reflection or a protected setter if you want to avoid it
        // For this example, we assume we can construct it with the ID.
        CartItem initialItem = new CartItem(variant, 2);
        cart = new ShoppingCart(cartId, userId, new CartItems(List.of(initialItem)), CartStatus.ACTIVE, Instant.now(), Instant.now());


        when(cartPort.findById(cartId)).thenReturn(Optional.of(cart));
        when(variantPort.findById(variantId)).thenReturn(Optional.of(variant));
        when(cartPort.save(any(ShoppingCart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Mock the remove use case behavior
        when(removeCartItemUseCase.execute(cartId, variantId)).thenAnswer(invocation -> {
            cart.updateItemQuantity(variant, 0); // Simulate removal
            return cartPort.save(cart);
        });
    }

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

    @Test
    void execute_shouldRemoveItem_whenQuantityIsZero() {
        int newQuantity = 0;

        ShoppingCart updatedCart = updateCartItemUseCase.execute(cartId, variantId, newQuantity);

        assertNotNull(updatedCart);
        assertTrue(updatedCart.getItems().isEmpty());

        // Verify the correct flow for quantity 0
        verify(removeCartItemUseCase).execute(cartId, variantId);
        verify(cartPort, never()).findById(cartId); // <<< FIX: This is never called in the quantity=0 path
        verify(variantPort, never()).findById(variantId);
    }


    @Test
    void execute_shouldThrowException_whenQuantityIsNegative() {
        assertThrows(IllegalArgumentException.class, () -> {
            updateCartItemUseCase.execute(cartId, variantId, -1);
        });
    }

     @Test
    void execute_shouldThrowException_whenCartNotFound() {
        when(cartPort.findById(cartId)).thenReturn(Optional.empty());
        assertThrows(CartNotFoundException.class, () -> {
            updateCartItemUseCase.execute(cartId, variantId, 1);
        });
    }

    @Test
    void execute_shouldThrowException_whenVariantNotFound() {
        when(variantPort.findById(variantId)).thenReturn(Optional.empty());
        assertThrows(VariantNotFoundException.class, () -> {
            updateCartItemUseCase.execute(cartId, variantId, 1);
        });
        verify(removeCartItemUseCase, never()).execute(any(), any());
    }

    @Test
    void execute_shouldThrowException_whenInsufficientStockForIncrease() {
        // Current quantity is 2, available stock is 10.
        // New quantity of 13 requires 11 more items (13 - 2), but only 10 are available.
        int newQuantity = 13; // <<< FIX: This value now correctly causes a stock failure

        assertThrows(InsufficientStockException.class, () -> {
            updateCartItemUseCase.execute(cartId, variantId, newQuantity);
        });

        // Verify save was NOT called because the transaction should fail
        verify(cartPort, never()).save(any(ShoppingCart.class));
    }

    @Test
    void execute_shouldAllowDecreaseQuantity_evenWithInsufficientStock() {
         int newQuantity = 1; // Decrease from 2 to 1, no stock check needed
         ShoppingCart updatedCart = updateCartItemUseCase.execute(cartId, variantId, newQuantity);

         assertNotNull(updatedCart);
         assertEquals(1, updatedCart.getItems().size());
         assertEquals(newQuantity, updatedCart.getItems().get(0).getQuantity());
         verify(cartPort).save(cart);
    }
}