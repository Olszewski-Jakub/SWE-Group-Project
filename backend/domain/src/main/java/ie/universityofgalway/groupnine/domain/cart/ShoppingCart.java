package ie.universityofgalway.groupnine.domain.cart;

import ie.universityofgalway.groupnine.domain.product.Money;
import ie.universityofgalway.groupnine.domain.product.Variant;
import ie.universityofgalway.groupnine.domain.user.UserId;

import java.time.Instant;
import java.util.Currency;
import java.util.Objects;

/**
 * Aggregate root representing a customer's shopping cart.
 * <p>
 * Encapsulates cart lifecycle, validation, and item management.
 * Modifications are only allowed when the cart is {@link CartStatus#ACTIVE}.
 */
public class ShoppingCart {
    private final CartId id;
    private final UserId userId;
    private final CartItems items;
    private CartStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    /**
     * Constructs a fully-initialized shopping cart.
     */
    public ShoppingCart(CartId id, UserId userId, CartItems items, CartStatus status, Instant createdAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id);
        this.userId = Objects.requireNonNull(userId);
        this.items = Objects.requireNonNull(items);
        this.status = Objects.requireNonNull(status);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public CartId id() {
        return id;
    }

    public UserId userId() {
        return userId;
    }

    public CartStatus status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    public CartItems items() {
        return items;
    }

    /**
     * Adds an item; only allowed if the cart is active and enough stock exists.
     */
    public void addItem(Variant variant, int quantity) {
        modify(() -> {
            if (quantity > variant.stock().available()) {
                throw new InsufficientStockException(
                        "Cannot add " + quantity + " units of variant " + variant.id() +
                                " — only " + variant.stock().available() + " available"
                );
            }
            items.add(variant, quantity);
        });
    }

    /**
     * Removes an item.
     */
    public void removeItem(Variant variant) {
        modify(() -> items.remove(variant));
    }

    /**
     * Updates an item's quantity, checking stock if increasing.
     */
    public void updateItemQuantity(Variant variant, int newQuantity) {
        modify(() -> {
            int currentQuantity = items.getQuantity(variant);
            int delta = newQuantity - currentQuantity;
            if (delta > 0 && delta > variant.stock().available()) {
                throw new InsufficientStockException(
                        "Cannot increase to " + newQuantity + " units of variant " + variant.id() +
                                " — only " + variant.stock().available() + " available"
                );
            }
            items.updateQuantity(variant, newQuantity);
        });
    }

    public void clear() {
        modify(items::clear);
    }

    /**
     * Marks the cart as checked out.
     */
    public void checkout() {
        status = CartStatus.CHECKED_OUT;
        updatedAt = Instant.now();
    }

    /**
     * Marks the cart as abandoned.
     */
    public void abandon() {
        status = CartStatus.ABANDONED;
        updatedAt = Instant.now();
    }

    /**
     * @return total number of individual items in the cart
     */
    public int getTotalItems() {
        return items.asList().stream().mapToInt(CartItem::getQuantity).sum();
    }

    /**
     * Calculates the cart's total value by summing all item subtotals.
     * Defaults to zero EUR if the cart is empty.
     */
    public Money total() {
        if (items.isEmpty()) {
            return new Money(java.math.BigDecimal.ZERO, Currency.getInstance("EUR"));
        }

        return items.asList().stream()
                .map(CartItem::subtotal)
                .reduce(Money::add)
                .orElse(new Money(java.math.BigDecimal.ZERO, Currency.getInstance("EUR")));
    }

    private void modify(Runnable action) {
        ensureActive();
        action.run();
        updatedAt = Instant.now();
    }

    private void ensureActive() {
        if (status != CartStatus.ACTIVE) {
            throw new IllegalStateException("Cannot modify cart with status: " + status);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShoppingCart that)) return false;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Factory method for creating a new, empty cart for a user.
     * Sets default status to ACTIVE and initializes empty items.
     */
    public static ShoppingCart createNew(UserId userId) {
        Instant now = Instant.now();
        return new ShoppingCart(
                new CartId(java.util.UUID.randomUUID()),
                userId,
                new CartItems(),
                CartStatus.ACTIVE,
                now,
                now
        );
    }

}
