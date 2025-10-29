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
 * Unit tests for the cart domain model, including {@link CartItem}, {@link CartItems},
 * and {@link ShoppingCart}.
 */
class CartItemTest {

    private Variant variant1;
    private Variant variant2;
    private Currency eur;

    /**
     * Initializes reusable {@link Variant} instances with sample data before each test.
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
     * Verifies that {@link CartId} can be created from a UUID or a valid string
     * representation and that invalid strings are rejected.
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
     * Tests the constructor validation of {@link CartItem} and verifies that the
     * subtotal calculation is correct.
     */
    @Test
    void cartItem_validation_andSubtotal() {
        CartItem item = new CartItem(variant1, 2);
        assertEquals(variant1, item.getVariant());
        assertEquals(2, item.getQuantity());
        assertEquals(new Money(new BigDecimal("20.00"), eur), item.subtotal());
        assertThrows(NullPointerException.class, () -> new CartItem(null, 1));
        assertThrows(IllegalArgumentException.class, () -> new CartItem(variant1, 0));
        assertThrows(IllegalArgumentException.class, () -> new CartItem(variant1, -1));
    }

    /**
     * Verifies that {@link CartItem} quantity can be correctly increased and
     * decreased, and that invalid operations throw exceptions.
     */
    @Test
    void cartItem_increaseDecreaseQuantity() {
        CartItem item = new CartItem(variant1, 2);
        CartItem increased = item.increaseQuantity(3);
        assertEquals(5, increased.getQuantity());
        assertThrows(IllegalArgumentException.class, () -> item.increaseQuantity(0));
        assertThrows(IllegalArgumentException.class, () -> item.increaseQuantity(-1));

        CartItem decreased = increased.decreaseQuantity(2);
        assertEquals(3, decreased.getQuantity());
        assertThrows(IllegalArgumentException.class, () -> decreased.decreaseQuantity(0));
        assertThrows(IllegalArgumentException.class, () -> decreased.decreaseQuantity(-1));
        assertThrows(IllegalArgumentException.class, () -> decreased.decreaseQuantity(4));
    }

    /**
     * Tests the core operations of the {@link CartItems} collection, including
     * adding, merging, removing, and clearing items, as well as enforcing
     * currency consistency.
     */
    @Test
    void cartItems_addAndRemove() {
        CartItems cartItems = new CartItems();
        cartItems.add(variant1, 2);
        assertTrue(cartItems.hasItem(variant1));
        assertEquals(1, cartItems.asList().size());
        assertEquals(2, cartItems.getQuantity(variant1));

        cartItems.add(variant1, 3);
        assertEquals(5, cartItems.getQuantity(variant1));
        assertEquals(1, cartItems.asList().size());

        cartItems.add(variant2, 1);
        assertEquals(2, cartItems.asList().size());
        assertEquals(1, cartItems.getQuantity(variant2));

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
        assertEquals(0, cartItems.getQuantity(variant1));
        assertEquals(1, cartItems.asList().size());

        cartItems.clear();
        assertTrue(cartItems.isEmpty());
        assertEquals(0, cartItems.asList().size());
        assertNull(cartItems.getCartCurrency());
    }

    /**
     * Verifies that updating an item's quantity in {@link CartItems} correctly
     * adds, modifies, or removes the item and manages the cart currency.
     */
    @Test
    void cartItems_updateQuantity() {
        CartItems cartItems = new CartItems();
        cartItems.add(variant1, 2);
        assertEquals(eur, cartItems.getCartCurrency());

        cartItems.updateQuantity(variant1, 5);
        assertEquals(5, cartItems.getQuantity(variant1));
        assertEquals(eur, cartItems.getCartCurrency());

        cartItems.updateQuantity(variant2, 3);
        assertTrue(cartItems.hasItem(variant2));
        assertEquals(3, cartItems.getQuantity(variant2));
        assertEquals(2, cartItems.asList().size());

        cartItems.updateQuantity(variant1, 0);
        assertFalse(cartItems.hasItem(variant1));
        assertEquals(1, cartItems.asList().size());

        cartItems.updateQuantity(variant2, 0);
        assertTrue(cartItems.isEmpty());
        assertNull(cartItems.getCartCurrency());

        cartItems.updateQuantity(variant1, 1);
        assertEquals(1, cartItems.getQuantity(variant1));
        assertEquals(eur, cartItems.getCartCurrency());

        assertThrows(IllegalArgumentException.class, () -> cartItems.updateQuantity(variant1, -1));
    }

    /**
     * Verifies the core item manipulation and calculation methods of the
     * {@link ShoppingCart} aggregate root.
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
        assertEquals(5, cart.getItems().stream().filter(i -> i.getVariant().equals(variant1)).findFirst().get().getQuantity());

        cart.removeItem(variant2);
        assertFalse(cart.getItems().stream().anyMatch(item -> item.getVariant().equals(variant2)));
        assertEquals(1, cart.getItems().size());

        cart.clear();
        assertTrue(cart.getItems().isEmpty());
    }

    /**
     * Verifies the lifecycle state transitions of a {@link ShoppingCart}, ensuring
     * that modifications are disallowed after checkout or abandonment.
     */
    @Test
    void shoppingCart_checkoutAndAbandon() {
        UserId userId = UserId.of(UUID.randomUUID());
        ShoppingCart cart = ShoppingCart.createNew(userId);

        cart.checkout();
        assertEquals(CartStatus.CHECKED_OUT, cart.getStatus());
        assertThrows(IllegalStateException.class, () -> cart.addItem(variant1, 1));

        ShoppingCart abandonedCart = ShoppingCart.createNew(userId);
        abandonedCart.abandon();
        assertEquals(CartStatus.ABANDONED, abandonedCart.getStatus());
        assertThrows(IllegalStateException.class, () -> abandonedCart.removeItem(variant1));
    }

    /**
     * Confirms that the {@link CartStatus} enum contains all expected values.
     */
    @Test
    void cartStatus_enumValues() {
        assertArrayEquals(new CartStatus[]{CartStatus.ACTIVE, CartStatus.CHECKED_OUT, CartStatus.ABANDONED}, CartStatus.values());
    }
}