package ie.universityofgalway.groupnine.domain.cart;

import ie.universityofgalway.groupnine.domain.product.Variant;

import java.util.*;

/**
 * Represents a collection of CartItems inside a shopping cart.
 */
public class CartItems {

    private final List<CartItem> items = new ArrayList<>();
    private Currency cartCurrency = null;

    public CartItems() {}

    public CartItems(List<CartItem> initialItems) {
        if (initialItems != null) {
            items.addAll(initialItems);
            if (!items.isEmpty()) {
                cartCurrency = items.get(0).getVariant().price().currency();
            }
        }
    }

    public List<CartItem> asList() {
        return Collections.unmodifiableList(new ArrayList<>(items));
    }

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

    public void remove(Variant variant) {
        Objects.requireNonNull(variant, "variant cannot be null");
        int idx = indexOfVariant(variant);
        if (idx >= 0) {
            items.remove(idx);
            if (items.isEmpty()) cartCurrency = null;
        }
    }

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

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public void clear() {
        items.clear();
        cartCurrency = null;
    }

    public boolean hasItem(Variant variant) {
        return indexOfVariant(variant) >= 0;
    }

    public int getQuantity(Variant variant) {
        int idx = indexOfVariant(variant);
        if (idx >= 0) {
            return items.get(idx).getQuantity();
        }
        return 0;
    }

    private int indexOfVariant(Variant variant) {
        Object id = variant.id();
        for (int i = 0; i < items.size(); i++) {
            if (Objects.equals(items.get(i).getVariant().id(), id)) {
                return i;
            }
        }
        return -1;
    }
}