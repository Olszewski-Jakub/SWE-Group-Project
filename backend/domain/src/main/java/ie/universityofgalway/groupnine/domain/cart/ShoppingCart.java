package ie.universityofgalway.groupnine.domain.cart;

import ie.universityofgalway.groupnine.domain.product.Money;
import ie.universityofgalway.groupnine.domain.product.Stock; // Import Stock
import ie.universityofgalway.groupnine.domain.product.Variant;
import ie.universityofgalway.groupnine.domain.user.UserId;

import java.time.Instant;
import java.util.Currency;
import java.util.List; // Import List
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
    private final CartItems items; // Now managed by CartItems class
    private CartStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    /**
     * Constructs a fully-initialized shopping cart.
     */
    public ShoppingCart(CartId id, UserId userId, CartItems items, CartStatus status, Instant createdAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "Cart ID cannot be null");
        this.userId = Objects.requireNonNull(userId, "User ID cannot be null");
        this.items = Objects.requireNonNull(items, "Cart items cannot be null");
        this.status = Objects.requireNonNull(status, "Cart status cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "Creation timestamp cannot be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "Update timestamp cannot be null");
    }

    // --- Getters ---
    public CartId getId() { return id; }
    public UserId getUserId() { return userId; }
    public CartStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public List<CartItem> getItems() { return items.asList(); } // Expose items via CartItems

    /**
     * Adds an item; only allowed if the cart is active and enough stock exists.
     * Delegates currency validation to CartItems.
     *
     * @param variant  The variant to add.
     * @param quantity The positive quantity to add.
     * @throws InsufficientStockException if not enough stock is available.
     * @throws IllegalStateException if cart is not ACTIVE.
     * @throws IllegalArgumentException if quantity <= 0 or currency mixes.
     */
    public void addItem(Variant variant, int quantity) {
        modify(() -> {
            // FIX: Access stock details correctly
            Stock stock = variant.getStock();
            int availableStock = stock.getQuantity() - stock.getReserved(); // Calculate available
            if (quantity > availableStock) {
                throw new InsufficientStockException(
                        "Cannot add " + quantity + " units of variant " + variant.getId() +
                                " — only " + availableStock + " available"
                );
            }
            items.add(variant, quantity);
        });
    }

    /**
     * Removes an item entirely from the cart.
     * Allowed only if the cart is active.
     *
     * @param variant The variant to remove.
     * @throws IllegalStateException if cart is not ACTIVE.
     */
    public void removeItem(Variant variant) {
        modify(() -> items.remove(variant));
    }

    /**
     * Updates an item's quantity. Checks stock if increasing quantity.
     * Allowed only if the cart is active.
     *
     * @param variant     The variant to update.
     * @param newQuantity The non-negative target quantity.
     * @throws InsufficientStockException if increasing quantity exceeds available stock.
     * @throws IllegalStateException if cart is not ACTIVE.
     * @throws IllegalArgumentException if newQuantity < 0.
     */
    public void updateItemQuantity(Variant variant, int newQuantity) {
        modify(() -> {
            int currentQuantity = items.getQuantity(variant);
            int delta = newQuantity - currentQuantity;

            // Check stock only if we are increasing the quantity
            if (delta > 0) {
                // FIX: Access stock details correctly
                Stock stock = variant.getStock();
                int availableStock = stock.getQuantity() - stock.getReserved(); // Calculate available
                if (delta > availableStock) {
                    throw new InsufficientStockException(
                            "Cannot increase quantity by " + delta + " for variant " + variant.getId() +
                                    " — only " + availableStock + " more available (current: " + currentQuantity + ")"
                    );
                }
            }
            // Delegate the actual update (or add/remove) to CartItems
            items.updateQuantity(variant, newQuantity);
        });
    }

    /**
     * Clears all items from the cart.
     * Allowed only if the cart is active.
     * @throws IllegalStateException if cart is not ACTIVE.
     */
    public void clear() {
        modify(items::clear);
    }

    // --- Status Transitions ---

    /**
     * Marks the cart as checked out. Cart can no longer be modified.
     */
    public void checkout() {
        if (status == CartStatus.ACTIVE) {
            status = CartStatus.CHECKED_OUT;
            touch(); // Update timestamp
        } else {
            // Optionally log or throw if trying to checkout a non-active cart
            System.err.println("Warning: Tried to checkout cart " + id + " with status " + status);
        }
    }

    /**
     * Marks the cart as abandoned. Cart can no longer be modified.
     */
    public void abandon() {
         if (status == CartStatus.ACTIVE) {
            status = CartStatus.ABANDONED;
            touch(); // Update timestamp
        } else {
            System.err.println("Warning: Tried to abandon cart " + id + " with status " + status);
        }
    }

    // --- Read Operations ---

    /**
     * @return total number of individual item units in the cart (sum of quantities).
     */
    public int getTotalItems() {
        return items.asList().stream().mapToInt(CartItem::getQuantity).sum();
    }

    /**
     * Calculates the cart's total value by summing all item subtotals.
     * Returns zero amount in the cart's currency (or EUR if empty).
     * @return The total monetary value of the cart.
     */
    public Money total() {
        Currency currency = items.getCartCurrency();
        if (currency == null) {
            // Default to EUR if cart is empty and has no currency set
            currency = Currency.getInstance("EUR");
        }

        Money zero = new Money(java.math.BigDecimal.ZERO, currency);

        // Calculate sum, ensuring all items have the expected currency (should be guaranteed by CartItems)
        return items.asList().stream()
                .map(CartItem::subtotal)
                .reduce(zero, Money::add); // Start reduction with zero of the correct currency
    }

    // --- Internal Helpers ---

    /**
     * Wraps modification logic, ensuring the cart is active and updating the timestamp.
     * @param action The modification to perform.
     */
    private void modify(Runnable action) {
        ensureActive();
        action.run();
        touch();
    }

    /**
     * Throws IllegalStateException if the cart is not in ACTIVE status.
     */
    private void ensureActive() {
        if (status != CartStatus.ACTIVE) {
            throw new IllegalStateException("Cannot modify cart with status: " + status);
        }
    }

    /**
     * Updates the 'updatedAt' timestamp to the current time.
     */
    private void touch() {
        this.updatedAt = Instant.now();
    }

    // --- Equality & Hashing (Based on ID) ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShoppingCart that)) return false;
        // Equality is based solely on the CartId
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        // Hash code is based solely on the CartId
        return Objects.hash(id);
    }

    // --- Factory Method ---

    /**
     * Factory method for creating a new, empty cart for a user.
     * Sets default status to ACTIVE and initializes empty items.
     * @param userId The ID of the user owning the cart.
     * @return A new ShoppingCart instance.
     */
    public static ShoppingCart createNew(UserId userId) {
        Instant now = Instant.now();
        return new ShoppingCart(
                new CartId(java.util.UUID.randomUUID()),
                Objects.requireNonNull(userId, "User ID cannot be null for new cart"),
                new CartItems(), // Start with empty items
                CartStatus.ACTIVE,
                now,
                now
        );
    }
}

