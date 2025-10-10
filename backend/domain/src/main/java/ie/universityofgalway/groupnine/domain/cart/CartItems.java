package ie.universityofgalway.groupnine.domain.cart;

import ie.universityofgalway.groupnine.domain.product.Money;
import ie.universityofgalway.groupnine.domain.product.Variant;
import java.util.*;

/**
 * Represents a collection of {@link CartItem}s inside a shopping cart.
 * <p>
 * Enforces the following invariants:
 * <ul>
 *   <li>All items must share the same {@link java.util.Currency}</li>
 *   <li>Quantities must be strictly positive</li>
 * </ul>
 */
public class CartItems {
    private final List<CartItem> items = new ArrayList<>();
    private Currency cartCurrency = null;

    /**
     * Creates an empty list of cart items.
     */
    public CartItems() {}

    /**
     * Initializes cart items with an existing list.
     * @param initialItems list of existing items (may be null)
     */
    public CartItems(List<CartItem> initialItems) {
        if (initialItems != null) {
            items.addAll(initialItems);
            if (!items.isEmpty()) {
                cartCurrency = items.get(0).variant().price().currency();
            }
        }
    }

    /**
     * Returns an immutable copy of the items.
     */
    public List<CartItem> asList() {
        return Collections.unmodifiableList(new ArrayList<>(items));
    }

    /**
     * Adds a variant to the cart, merging quantities if it already exists.
     * @param variant product variant (non-null)
     * @param quantity quantity to add (> 0)
     * @throws IllegalArgumentException if mixing currencies or invalid quantity
     */
    public void add(Variant variant, int quantity) {
        Objects.requireNonNull(variant, "variant cannot be null");
        if (quantity <= 0) throw new IllegalArgumentException("quantity must be > 0");

        Currency variantCurrency = variant.price().currency();
        if (cartCurrency == null) {
            cartCurrency = variantCurrency;
        } else if (!cartCurrency.equals(variantCurrency)) {
            throw new IllegalArgumentException("Cannot mix currencies in cart");
        }

        int idx = indexOfVariant(variant);
        if (idx >= 0) {
            CartItem existing = items.get(idx);
            items.set(idx, existing.increaseQuantity(quantity));
        } else {
            items.add(new CartItem(variant, quantity));
        }
    }

    /**
     * Removes a variant entirely from the cart.
     */
    public void remove(Variant variant) {
        Objects.requireNonNull(variant, "variant cannot be null");
        int idx = indexOfVariant(variant);
        if (idx >= 0) {
            items.remove(idx);
            if (items.isEmpty()) cartCurrency = null;
        }
    }

    /**
     * Updates the quantity of a variant, adding or removing as needed.
     * If {@code newQuantity == 0}, the item is removed.
     */
    public void updateQuantity(Variant variant, int newQuantity) {
        Objects.requireNonNull(variant, "variant cannot be null");
        if (newQuantity < 0) throw new IllegalArgumentException("newQuantity must be >= 0");

        int idx = indexOfVariant(variant);
        if (idx < 0) {
            if (newQuantity > 0) add(variant, newQuantity);
            return;
        }

        if (newQuantity == 0) {
            items.remove(idx);
            if (items.isEmpty()) cartCurrency = null;
        } else {
            items.set(idx, new CartItem(variant, newQuantity));
        }
    }

    /** @return true if no items are in the cart */
    public boolean isEmpty() { return items.isEmpty(); }

    /** Removes all items and resets currency. */
    public void clear() {
        items.clear();
        cartCurrency = null;
    }

    /** Checks if a specific variant exists in the cart. */
    public boolean hasItem(Variant variant) {
        return indexOfVariant(variant) >= 0;
    }

    private int indexOfVariant(Variant variant) {
        Object id = variant.id();
        for (int i = 0; i < items.size(); i++) {
            if (Objects.equals(items.get(i).variant().id(), id)) {
                return i;
            }
        }
        return -1;
    }
}