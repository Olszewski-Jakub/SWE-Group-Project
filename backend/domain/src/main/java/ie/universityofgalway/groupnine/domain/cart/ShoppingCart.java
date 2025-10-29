package ie.universityofgalway.groupnine.domain.cart;

import ie.universityofgalway.groupnine.domain.cart.exception.InsufficientStockException;
import ie.universityofgalway.groupnine.domain.product.Money;
import ie.universityofgalway.groupnine.domain.product.Stock;
import ie.universityofgalway.groupnine.domain.product.Variant;
import ie.universityofgalway.groupnine.domain.user.UserId;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.Objects;

/**
 * An aggregate root representing a customer's shopping cart. This class
 * encapsulates the cart's lifecycle, state transitions, validation, and item management.
 * Modifications are only permitted when the cart is in an {@link CartStatus#ACTIVE} state.
 */
public class ShoppingCart {
    private final CartId id;
    private final UserId userId;
    private final CartItems items;
    private CartStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    /**
     * Constructs a fully-initialized shopping cart instance.
     *
     * @param id The unique identifier for the cart.
     * @param userId The identifier of the user who owns the cart.
     * @param items The collection of items in the cart.
     * @param status The current status of the cart.
     * @param createdAt The timestamp of creation.
     * @param updatedAt The timestamp of the last modification.
     */
    public ShoppingCart(CartId id, UserId userId, CartItems items, CartStatus status, Instant createdAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "Cart ID cannot be null");
        this.userId = Objects.requireNonNull(userId, "User ID cannot be null");
        this.items = Objects.requireNonNull(items, "Cart items cannot be null");
        this.status = Objects.requireNonNull(status, "Cart status cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "Creation timestamp cannot be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "Update timestamp cannot be null");
    }

    public CartId getId() { return id; }
    public UserId getUserId() { return userId; }
    public CartStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public List<CartItem> getItems() { return items.asList(); }

    /**
     * Adds a specified quantity of a product variant to the cart.
     *
     * @param variant The variant to add.
     * @param quantity The positive quantity to add.
     * @throws InsufficientStockException if the requested quantity exceeds available stock.
     * @throws IllegalStateException if the cart is not in an {@code ACTIVE} status.
     */
    public void addItem(Variant variant, int quantity) {
        modify(() -> {
            Stock stock = variant.getStock();
            int availableStock = stock.getQuantity() - stock.getReserved();
            if (quantity > availableStock) {
                throw new InsufficientStockException("Cannot add " + quantity + " units of variant " + variant.getId() + " — only " + availableStock + " available");
            }
            items.add(variant, quantity);
        });
    }

    /**
     * Removes a product variant entirely from the cart.
     *
     * @param variant The variant to remove.
     * @throws IllegalStateException if the cart is not in an {@code ACTIVE} status.
     */
    public void removeItem(Variant variant) {
        modify(() -> items.remove(variant));
    }

    /**
     * Updates the quantity of a product variant in the cart. If the quantity is
     * increased, a stock check is performed.
     *
     * @param variant The variant to update.
     * @param newQuantity The non-negative target quantity.
     * @throws InsufficientStockException if increasing the quantity exceeds available stock.
     * @throws IllegalStateException if the cart is not in an {@code ACTIVE} status.
     */
    public void updateItemQuantity(Variant variant, int newQuantity) {
        modify(() -> {
            int currentQuantity = items.getQuantity(variant);
            int delta = newQuantity - currentQuantity;

            if (delta > 0) {
                Stock stock = variant.getStock();
                int availableStock = stock.getQuantity() - stock.getReserved();
                if (delta > availableStock) {
                    throw new InsufficientStockException("Cannot increase quantity by " + delta + " for variant " + variant.getId() + " — only " + availableStock + " more available (current: " + currentQuantity + ")");
                }
            }
            items.updateQuantity(variant, newQuantity);
        });
    }

    /**
     * Removes all items from the cart.
     *
     * @throws IllegalStateException if the cart is not in an {@code ACTIVE} status.
     */
    public void clear() {
        modify(items::clear);
    }

    /**
     * Transitions the cart's status to {@code CHECKED_OUT}. Once checked out,
     * the cart can no longer be modified.
     */
    public void checkout() {
        if (status == CartStatus.ACTIVE) {
            status = CartStatus.CHECKED_OUT;
            touch();
        }
    }

    /**
     * Transitions the cart's status to {@code ABANDONED}. Once abandoned,
     * the cart can no longer be modified.
     */
    public void abandon() {
         if (status == CartStatus.ACTIVE) {
            status = CartStatus.ABANDONED;
            touch();
        }
    }

    /**
     * Calculates the total number of individual item units in the cart.
     *
     * @return The sum of all item quantities.
     */
    public int getTotalItems() {
        return items.asList().stream().mapToInt(CartItem::getQuantity).sum();
    }

    /**
     * Calculates the total monetary value of all items in the cart.
     *
     * @return A {@link Money} object representing the cart's total value.
     * If the cart is empty, it returns zero in the default currency (EUR).
     */
    public Money total() {
        Currency currency = items.getCartCurrency();
        if (currency == null) {
            currency = Currency.getInstance("EUR");
        }
        Money zero = new Money(java.math.BigDecimal.ZERO, currency);
        return items.asList().stream()
                .map(CartItem::subtotal)
                .reduce(zero, Money::add);
    }

    private void modify(Runnable action) {
        ensureActive();
        action.run();
        touch();
    }

    private void ensureActive() {
        if (status != CartStatus.ACTIVE) {
            throw new IllegalStateException("Cannot modify cart with status: " + status);
        }
    }

    private void touch() {
        this.updatedAt = Instant.now();
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
     * A factory method to create a new, empty shopping cart for a given user.
     *
     * @param userId The {@link UserId} of the user who will own the new cart.
     * @return A new {@link ShoppingCart} instance with an {@code ACTIVE} status.
     */
    public static ShoppingCart createNew(UserId userId) {
        Instant now = Instant.now();
        return new ShoppingCart(
                new CartId(java.util.UUID.randomUUID()),
                Objects.requireNonNull(userId, "User ID cannot be null for new cart"),
                new CartItems(),
                CartStatus.ACTIVE,
                now,
                now
        );
    }
}