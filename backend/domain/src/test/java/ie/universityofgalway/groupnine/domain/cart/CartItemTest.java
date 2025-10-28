package ie.universityofgalway.groupnine.domain.cart;

import ie.universityofgalway.groupnine.domain.product.*;
import ie.universityofgalway.groupnine.domain.user.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the cart domain model, updated for class-based domain objects.
 */
class CartItemTest {

    private Variant variant1;
    private Variant variant2;
    private Currency eur;

    /**
     * Sets up reusable {@link Variant} instances with sample data before each test.
     */
    @BeforeEach
    void setUp() {
        eur = Currency.getInstance("EUR");

        // Use class constructors now
        VariantId variantId1 = new VariantId(UUID.randomUUID());
        VariantId variantId2 = new VariantId(UUID.randomUUID());
        Sku sku1 = new Sku("SKU1");
        Sku sku2 = new Sku("SKU2");
        Stock stock = new Stock(100, 0);
        List<Attribute> attributes = List.of();

        variant1 = new Variant(variantId1, sku1, new Money(new BigDecimal("10.00"), eur), stock, attributes);
        variant2 = new Variant(variantId2, sku2, new Money(new BigDecimal("20.00"), eur), stock, attributes);
    }

    /**
     * Verifies CartId equality and validation.
     */
    @Test
    void cartId_fromStringAndUUID_shouldBeEqual() {
        UUID uuid = UUID.randomUUID();
        CartId id1 = new CartId(uuid);
        CartId id2 = new CartId(uuid.toString());
        assertEquals(id1, id2);
        assertThrows(IllegalArgumentException.class, () -> new CartId("invalid-uuid"));
    }

    /**
     * Tests CartItem validation and subtotal calculation using getters.
     */
    @Test
    void cartItem_validation_andSubtotal() {
        CartItem item = new CartItem(variant1, 2);
        assertEquals(variant1, item.getVariant());
        assertEquals(2, item.getQuantity());
        // Use Money's equals method for comparison
        assertEquals(new Money(new BigDecimal("20.00"), eur), item.subtotal());

        assertThrows(NullPointerException.class, () -> new CartItem(null, 1));
        assertThrows(IllegalArgumentException.class, () -> new CartItem(variant1, 0));
        assertThrows(IllegalArgumentException.class, () -> new CartItem(variant1, -1)); // Added test for negative quantity
    }

    /**
     * Verifies CartItem quantity increase/decrease using getters.
     */
    @Test
    void cartItem_increaseDecreaseQuantity() {
        CartItem item = new CartItem(variant1, 2);
        CartItem increased = item.increaseQuantity(3);
        assertEquals(5, increased.getQuantity());
        assertThrows(IllegalArgumentException.class, () -> item.increaseQuantity(0));
        assertThrows(IllegalArgumentException.class, () -> item.increaseQuantity(-1)); // Added test for negative increase

        CartItem decreased = increased.decreaseQuantity(2);
        assertEquals(3, decreased.getQuantity());
        assertThrows(IllegalArgumentException.class, () -> decreased.decreaseQuantity(0));
        assertThrows(IllegalArgumentException.class, () -> decreased.decreaseQuantity(-1)); // Added test for negative decrease
        assertThrows(IllegalArgumentException.class, () -> decreased.decreaseQuantity(4)); // Test decreasing more than available
        assertThrows(IllegalArgumentException.class, () -> decreased.decreaseQuantity(10));
    }

    /**
     * Tests CartItems add, remove, clear, using getters.
     */
    @Test
    void cartItems_addAndRemove() {
        CartItems cartItems = new CartItems();
        cartItems.add(variant1, 2);
        assertTrue(cartItems.hasItem(variant1));
        assertEquals(1, cartItems.asList().size());
        assertEquals(2, cartItems.getQuantity(variant1)); // Check quantity

        cartItems.add(variant1, 3);
        assertEquals(5, cartItems.getQuantity(variant1)); // Check merged quantity
        assertEquals(1, cartItems.asList().size()); // Size should still be 1

        cartItems.add(variant2, 1);
        assertEquals(2, cartItems.asList().size());
        assertEquals(1, cartItems.getQuantity(variant2));

        // Mixing currencies should throw
        Variant usdVariant = new Variant(
                new VariantId(UUID.randomUUID()),
                new Sku("SKU-USD"),
                new Money(BigDecimal.TEN, Currency.getInstance("USD")),
                new Stock(10, 0),
                List.of()
        );
        assertThrows(IllegalArgumentException.class, () -> cartItems.add(usdVariant, 1));

        cartItems.remove(variant1);
        assertFalse(cartItems.hasItem(variant1));
        assertEquals(0, cartItems.getQuantity(variant1)); // Quantity should be 0
        assertEquals(1, cartItems.asList().size()); // Only variant2 left

        cartItems.clear();
        assertTrue(cartItems.isEmpty());
        assertEquals(0, cartItems.asList().size());
        assertNull(cartItems.getCartCurrency()); // Currency should reset
    }

    /**
     * Tests CartItems updateQuantity using getters.
     */
    @Test
    void cartItems_updateQuantity() {
        CartItems cartItems = new CartItems();
        cartItems.add(variant1, 2);
        assertEquals(eur, cartItems.getCartCurrency()); // Check currency is set

        cartItems.updateQuantity(variant1, 5);
        assertEquals(5, cartItems.getQuantity(variant1));
        assertEquals(eur, cartItems.getCartCurrency()); // Currency should remain

        // Update non-existent item should add it
        cartItems.updateQuantity(variant2, 3);
        assertTrue(cartItems.hasItem(variant2));
        assertEquals(3, cartItems.getQuantity(variant2));
        assertEquals(2, cartItems.asList().size());

        // Update quantity to zero should remove item
        cartItems.updateQuantity(variant1, 0);
        assertFalse(cartItems.hasItem(variant1));
        assertEquals(1, cartItems.asList().size());

        // Update last item to zero should remove it and clear currency
        cartItems.updateQuantity(variant2, 0);
        assertTrue(cartItems.isEmpty());
        assertNull(cartItems.getCartCurrency());

        // Test adding back after clear
        cartItems.updateQuantity(variant1, 1);
        assertEquals(1, cartItems.getQuantity(variant1));
        assertEquals(eur, cartItems.getCartCurrency());

        // Test invalid quantity
        assertThrows(IllegalArgumentException.class, () -> cartItems.updateQuantity(variant1, -1));
    }

    /**
     * Verifies ShoppingCart operations using getters.
     */
    @Test
    void shoppingCart_addRemoveUpdateClear() {
        UserId userId = UserId.of(UUID.randomUUID());
        ShoppingCart cart = ShoppingCart.createNew(userId);

        cart.addItem(variant1, 2);
        cart.addItem(variant2, 1);
        assertEquals(3, cart.getTotalItems());
        assertEquals(new Money(new BigDecimal("40.00"), Currency.getInstance("EUR")), cart.total());

        cart.updateItemQuantity(variant1, 5);
        assertEquals(6, cart.getTotalItems()); // 5 + 1
        assertEquals(5, cart.getItems().stream().filter(i -> i.getVariant().equals(variant1)).findFirst().get().getQuantity());

        cart.removeItem(variant2);
        // FIX: Access items via getItems() and check the contained variants
        assertFalse(cart.getItems().stream().anyMatch(item -> item.getVariant().equals(variant2)));
        assertEquals(1, cart.getItems().size()); // Only variant1 left

        cart.clear();
        assertTrue(cart.getItems().isEmpty());
    }

    /**
     * Tests ShoppingCart state transitions using getters.
     */
    @Test
    void shoppingCart_checkoutAndAbandon() {
        UserId userId = UserId.of(UUID.randomUUID());
        ShoppingCart cart = ShoppingCart.createNew(userId);

        cart.checkout();
        // FIX: Changed cart.status() to cart.getStatus()
        assertEquals(CartStatus.CHECKED_OUT, cart.getStatus());
        assertThrows(IllegalStateException.class, () -> cart.addItem(variant1, 1));

        ShoppingCart abandonedCart = ShoppingCart.createNew(userId);
        abandonedCart.abandon();
        // FIX: Changed abandonedCart.status() to abandonedCart.getStatus()
        assertEquals(CartStatus.ABANDONED, abandonedCart.getStatus());
        assertThrows(IllegalStateException.class, () -> abandonedCart.removeItem(variant1));
    }

    /**
     * Ensures CartStatus enum defines all expected values.
     */
    @Test
    void cartStatus_enumValues() {
        assertArrayEquals(new CartStatus[]{CartStatus.ACTIVE, CartStatus.CHECKED_OUT, CartStatus.ABANDONED}, CartStatus.values());
    }
}
