package ie.universityofgalway.groupnine.domain.cart;

import ie.universityofgalway.groupnine.domain.product.Variant;
import ie.universityofgalway.groupnine.domain.product.VariantId;
import java.util.*;

/**
 * A value object representing a collection of {@link CartItem}s within a shopping cart.
 * It manages item aggregation, enforces currency consistency, and provides methods
 * for manipulating the collection.
 */
public class CartItems {

    private final Map<VariantId, CartItem> items = new LinkedHashMap<>();
    private Currency cartCurrency = null;

    /**
     * Constructs an empty collection of cart items.
     */
    public CartItems() {}

    /**
     * Constructs a collection of cart items from an initial list.
     * It enforces that all items in the list share the same currency.
     *
     * @param initialItems A list of items to initialize the collection with.
     * @throws IllegalArgumentException if the provided items have mixed currencies.
     */
    public CartItems(List<CartItem> initialItems) {
        if (initialItems != null && !initialItems.isEmpty()) {
            cartCurrency = initialItems.get(0).getVariant().getPrice().getCurrency();
            for (CartItem item : initialItems) {
                if (!cartCurrency.equals(item.getVariant().getPrice().getCurrency())) {
                    throw new IllegalArgumentException("Cannot initialize CartItems with mixed currencies");
                }
                items.put(item.getVariant().getId(), item);
            }
        }
    }

    /**
     * Returns the items in the collection as an unmodifiable list.
     *
     * @return An unmodifiable {@link List} of {@link CartItem}s.
     */
    public List<CartItem> asList() {
        return Collections.unmodifiableList(new ArrayList<>(items.values()));
    }

    /**
     * Adds a specified quantity of a variant to the collection. If the variant
     * already exists, its quantity is increased. The currency of the first item
     * added sets the currency for the entire collection.
     *
     * @param variant The {@link Variant} to add.
     * @param quantity The positive quantity to add.
     * @throws NullPointerException if the variant is null.
     * @throws IllegalArgumentException if quantity is not positive or if the variant's
     * currency conflicts with the established cart currency.
     */
    public void add(Variant variant, int quantity) {
        Objects.requireNonNull(variant, "variant cannot be null");
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be > 0");
        }

        Currency variantCurrency = variant.getPrice().getCurrency();
        if (cartCurrency == null) {
            cartCurrency = variantCurrency;
        } else if (!cartCurrency.equals(variantCurrency)) {
            throw new IllegalArgumentException("Cannot mix currencies in cart. Expected " + cartCurrency + ", got " + variantCurrency);
        }

        VariantId variantId = variant.getId();
        CartItem existingItem = items.get(variantId);

        if (existingItem != null) {
            items.put(variantId, existingItem.increaseQuantity(quantity));
        } else {
            items.put(variantId, new CartItem(variant, quantity));
        }
    }

    /**
     * Removes a variant entirely from the collection. If this operation results
     * in an empty collection, the cart's currency is reset to null.
     *
     * @param variant The {@link Variant} to remove.
     */
    public void remove(Variant variant) {
        Objects.requireNonNull(variant, "variant cannot be null");
        if (items.remove(variant.getId()) != null) {
            if (items.isEmpty()) {
                cartCurrency = null;
            }
        }
    }

    /**
     * Updates the quantity of a variant in the collection. If the new quantity
     * is zero, the item is removed. If the item does not exist and the quantity
     * is positive, it is added.
     *
     * @param variant The {@link Variant} to update.
     * @param newQuantity The non-negative target quantity for the variant.
     * @throws NullPointerException if the variant is null.
     * @throws IllegalArgumentException if newQuantity is negative.
     */
    public void updateQuantity(Variant variant, int newQuantity) {
        Objects.requireNonNull(variant, "variant cannot be null");
        if (newQuantity < 0) {
            throw new IllegalArgumentException("newQuantity must be >= 0");
        }

        VariantId variantId = variant.getId();

        if (newQuantity == 0) {
            if (items.remove(variantId) != null && items.isEmpty()) {
                cartCurrency = null;
            }
        } else {
            if (items.containsKey(variantId)) {
                items.put(variantId, new CartItem(variant, newQuantity));
            } else {
                add(variant, newQuantity);
            }
        }
    }

    /**
     * Checks if the collection contains any items.
     *
     * @return {@code true} if the collection is empty, {@code false} otherwise.
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * Removes all items from the collection and resets the cart currency.
     */
    public void clear() {
        items.clear();
        cartCurrency = null;
    }

    /**
     * Checks if a specific variant is present in the collection.
     *
     * @param variant The {@link Variant} to check for.
     * @return {@code true} if the variant exists in the collection.
     */
    public boolean hasItem(Variant variant) {
        return variant != null && items.containsKey(variant.getId());
    }

    /**
     * Retrieves the current quantity of a specific variant.
     *
     * @param variant The {@link Variant} to query.
     * @return The quantity of the variant, or 0 if it is not in the collection.
     */
    public int getQuantity(Variant variant) {
        CartItem item = variant != null ? items.get(variant.getId()) : null;
        return item != null ? item.getQuantity() : 0;
    }

    /**
     * Returns the currency associated with this collection of items.
     *
     * @return The {@link Currency}, or null if the collection is empty.
     */
    public Currency getCartCurrency() {
        return cartCurrency;
    }
}