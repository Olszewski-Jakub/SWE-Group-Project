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
 * Unit tests for the cart domain model, including:
 * <ul>
 *     <li>{@link CartId} — cart identifier behavior and validation</li>
 *     <li>{@link CartItem} — quantity, validation, and subtotal logic</li>
 *     <li>{@link CartItems} — collection-level add, update, remove, and clear behavior</li>
 *     <li>{@link ShoppingCart} — aggregate operations, state transitions, and totals</li>
 *     <li>{@link CartStatus} — valid enum values</li>
 * </ul>
 *
 * <p>These tests verify the correctness of business rules in the shopping cart domain,
 * ensuring consistent behavior for cart manipulation and validation.</p>
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
     * Verifies that {@link CartId} instances created from the same UUID string and object are equal,
     * and that invalid UUID strings throw an exception.
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
     * Tests {@link CartItem} validation rules and subtotal calculation.
     */
    @Test
    void cartItem_validation_andSubtotal() {
        CartItem item = new CartItem(variant1, 2);
        assertEquals(variant1, item.getVariant());
        assertEquals(2, item.getQuantity());
        assertEquals(new Money(new BigDecimal("20.00"), eur), item.subtotal());

        assertThrows(NullPointerException.class, () -> new CartItem(null, 1));
        assertThrows(IllegalArgumentException.class, () -> new CartItem(variant1, 0));
    }

    /**
     * Verifies {@link CartItem} quantity increase and decrease operations with validation.
     */
    @Test
    void cartItem_increaseDecreaseQuantity() {
        CartItem item = new CartItem(variant1, 2);
        CartItem increased = item.increaseQuantity(3);
        assertEquals(5, increased.getQuantity());
        assertThrows(IllegalArgumentException.class, () -> item.increaseQuantity(0));

        CartItem decreased = increased.decreaseQuantity(2);
        assertEquals(3, decreased.getQuantity());
        assertThrows(IllegalArgumentException.class, () -> decreased.decreaseQuantity(0));
        assertThrows(IllegalArgumentException.class, () -> decreased.decreaseQuantity(10));
    }

    /**
     * Tests adding, removing, and clearing items in {@link CartItems},
     * including merging quantities and currency validation.
     */
    @Test
    void cartItems_addAndRemove() {
        CartItems cartItems = new CartItems();
        cartItems.add(variant1, 2);
        assertTrue(cartItems.hasItem(variant1));
        assertEquals(1, cartItems.asList().size());

        cartItems.add(variant1, 3);
        assertEquals(5, cartItems.asList().get(0).getQuantity());

        cartItems.add(variant2, 1);
        assertEquals(2, cartItems.asList().size());

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
        cartItems.clear();
        assertTrue(cartItems.isEmpty());
    }

    /**
     * Tests updating item quantities in {@link CartItems}, including removal when quantity becomes zero.
     */
    @Test
    void cartItems_updateQuantity() {
        CartItems cartItems = new CartItems();
        cartItems.add(variant1, 2);

        cartItems.updateQuantity(variant1, 5);
        assertEquals(5, cartItems.asList().get(0).getQuantity());

        cartItems.updateQuantity(variant1, 0);
        assertFalse(cartItems.hasItem(variant1));
    }

    /**
     * Verifies {@link ShoppingCart} operations such as add, update, remove, and clear.
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
        assertEquals(6, cart.getTotalItems());

        cart.removeItem(variant2);
        assertFalse(cart.items().hasItem(variant2));

        cart.clear();
        assertTrue(cart.items().isEmpty());
    }

    /**
     * Tests {@link ShoppingCart} state transitions to CHECKED_OUT and ABANDONED,
     * verifying that further modifications are disallowed after transition.
     */
    @Test
    void shoppingCart_checkoutAndAbandon() {
        UserId userId = UserId.of(UUID.randomUUID());
        ShoppingCart cart = ShoppingCart.createNew(userId);

        cart.checkout();
        assertEquals(CartStatus.CHECKED_OUT, cart.status());
        assertThrows(IllegalStateException.class, () -> cart.addItem(variant1, 1));

        ShoppingCart abandonedCart = ShoppingCart.createNew(userId);
        abandonedCart.abandon();
        assertEquals(CartStatus.ABANDONED, abandonedCart.status());
        assertThrows(IllegalStateException.class, () -> abandonedCart.removeItem(variant1));
    }

    /**
     * Ensures {@link CartStatus} enum defines all expected values.
     */
    @Test
    void cartStatus_enumValues() {
        assertArrayEquals(new CartStatus[]{CartStatus.ACTIVE, CartStatus.CHECKED_OUT, CartStatus.ABANDONED}, CartStatus.values());
    }
}