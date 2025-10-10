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
 * Unit tests for the shopping cart domain.
 * <p>
 * This class tests the core domain classes in ie.universityofgalway.groupnine.domain.cart:
 * <ul>
 *     <li>{@link CartId}</li>
 *     <li>{@link CartItem}</li>
 *     <li>{@link CartItems}</li>
 *     <li>{@link ShoppingCart}</li>
 *     <li>{@link CartStatus}</li>
 * </ul>
 *
 * Each test ensures the correctness of constructors, validations, item management, currency handling,
 * and cart lifecycle state transitions.
 */
class CartItemTest {

    private Variant variant1;
    private Variant variant2;
    private Currency eur;

    @BeforeEach
    void setUp() {
        eur = Currency.getInstance("EUR");

        // Variant dependencies
        final VariantId variantId1 = new VariantId(UUID.randomUUID());
        final VariantId variantId2 = new VariantId(UUID.randomUUID());
        final Sku sku1 = new Sku("SKU1");
        final Sku sku2 = new Sku("SKU2");
        final Stock stock = new Stock(100, 0);
        final List<Attribute> attributes = List.of();

        // Create two sample variants used in tests
        variant1 = new Variant(variantId1, sku1, new Money(new BigDecimal("10.00"), eur), stock, attributes);
        variant2 = new Variant(variantId2, sku2, new Money(new BigDecimal("20.00"), eur), stock, attributes);
    }

    /**
     * Tests the creation of {@link CartId} from both UUID and String.
     * Verifies equality and ensures invalid strings throw IllegalArgumentException.
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
     * Tests {@link CartItem} creation, validation, and subtotal computation.
     * Ensures that null variants or zero/negative quantities throw exceptions.
     */
    @Test
    void cartItem_validation_andSubtotal() {
        CartItem item = new CartItem(variant1, 2);
        assertEquals(variant1, item.variant());
        assertEquals(2, item.quantity());
        assertEquals(new Money(new BigDecimal("20.00"), eur), item.subtotal());

        assertThrows(NullPointerException.class, () -> new CartItem(null, 1));
        assertThrows(IllegalArgumentException.class, () -> new CartItem(variant1, 0));
    }

    /**
     * Tests increasing and decreasing quantities in {@link CartItem}.
     * Verifies new CartItem objects are created with updated quantities
     * and invalid operations throw exceptions.
     */
    @Test
    void cartItem_increaseDecreaseQuantity() {
        CartItem item = new CartItem(variant1, 2);
        CartItem increased = item.increaseQuantity(3);
        assertEquals(5, increased.quantity());
        assertThrows(IllegalArgumentException.class, () -> item.increaseQuantity(0));

        CartItem decreased = increased.decreaseQuantity(2);
        assertEquals(3, decreased.quantity());
        assertThrows(IllegalArgumentException.class, () -> decreased.decreaseQuantity(0));
        assertThrows(IllegalArgumentException.class, () -> decreased.decreaseQuantity(10));
    }

    /**
     * Tests adding, removing, and clearing items in {@link CartItems}.
     * Ensures quantities are merged when adding the same variant
     * and mixing currencies throws an exception.
     */
    @Test
    void cartItems_addAndRemove() {
        final CartItems cartItems = new CartItems();
        cartItems.add(variant1, 2);
        assertTrue(cartItems.hasItem(variant1));
        assertEquals(1, cartItems.asList().size());

        cartItems.add(variant1, 3);
        assertEquals(5, cartItems.asList().get(0).quantity());

        cartItems.add(variant2, 1);
        assertEquals(2, cartItems.asList().size());

        assertThrows(IllegalArgumentException.class, () -> {
            Variant usdVariant = new Variant(
                    new VariantId(UUID.randomUUID()),
                    new Sku("SKU-USD"),
                    new Money(BigDecimal.TEN, Currency.getInstance("USD")),
                    new Stock(10, 0),
                    List.of()
            );
            cartItems.add(usdVariant, 1);
        });

        cartItems.remove(variant1);
        assertFalse(cartItems.hasItem(variant1));
        cartItems.clear();
        assertTrue(cartItems.isEmpty());
    }

    /**
     * Tests updating the quantity of an existing variant in {@link CartItems}.
     * Verifies removal when the new quantity is set to zero.
     */
    @Test
    void cartItems_updateQuantity() {
        final CartItems cartItems = new CartItems();
        cartItems.add(variant1, 2);

        cartItems.updateQuantity(variant1, 5);
        assertEquals(5, cartItems.asList().get(0).quantity());

        cartItems.updateQuantity(variant1, 0);
        assertFalse(cartItems.hasItem(variant1));
    }

    /**
     * Tests adding, removing, updating, and clearing items in {@link ShoppingCart}.
     * Verifies that total item counts and total price calculations are correct.
     */
    @Test
    void shoppingCart_addRemoveUpdateClear() {
        final UserId userId = UserId.of(UUID.randomUUID());
        final ShoppingCart cart = new ShoppingCart(
                new CartId(UUID.randomUUID()),
                userId,
                new CartItems(),
                CartStatus.ACTIVE,
                Instant.now(),
                Instant.now()
        );

        cart.addItem(variant1, 2);
        cart.addItem(variant2, 1);
        assertEquals(3, cart.getTotalItems());
        assertEquals(new Money(new BigDecimal("40.00"), eur), cart.total());

        cart.updateItemQuantity(variant1, 5);
        assertEquals(6, cart.getTotalItems());

        cart.removeItem(variant2);
        assertFalse(cart.items().hasItem(variant2));

        cart.clear();
        assertTrue(cart.items().isEmpty());
    }

    /**
     * Tests cart lifecycle transitions: {@link CartStatus#CHECKED_OUT} and {@link CartStatus#ABANDONED}.
     * Verifies that modifying a cart after checkout or abandonment throws IllegalStateException.
     */
    @Test
    void shoppingCart_checkoutAndAbandon() {
        final UserId userId = UserId.of(UUID.randomUUID());
        final ShoppingCart cart = new ShoppingCart(
                new CartId(UUID.randomUUID()),
                userId,
                new CartItems(),
                CartStatus.ACTIVE,
                Instant.now(),
                Instant.now()
        );

        cart.checkout();
        assertEquals(CartStatus.CHECKED_OUT, cart.status());
        assertThrows(IllegalStateException.class, () -> cart.addItem(variant1, 1));

        final ShoppingCart abandonedCart = new ShoppingCart(
                new CartId(UUID.randomUUID()),
                userId,
                new CartItems(),
                CartStatus.ACTIVE,
                Instant.now(),
                Instant.now()
        );
        abandonedCart.abandon();
        assertEquals(CartStatus.ABANDONED, abandonedCart.status());
        assertThrows(IllegalStateException.class, () -> abandonedCart.removeItem(variant1));
    }

    /**
     * Tests the {@link CartStatus} enum values to ensure all expected states exist.
     */
    @Test
    void cartStatus_enumValues() {
        assertEquals(3, CartStatus.values().length);
        assertArrayEquals(new CartStatus[]{CartStatus.ACTIVE, CartStatus.CHECKED_OUT, CartStatus.ABANDONED}, CartStatus.values());
    }
}
