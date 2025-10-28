package ie.universityofgalway.groupnine.domain.cart;

import ie.universityofgalway.groupnine.domain.product.Variant;
import ie.universityofgalway.groupnine.domain.product.VariantId; // Import VariantId

import java.util.*;

/**
 * Represents a collection of CartItems inside a shopping cart.
 * Manages item aggregation and ensures currency consistency.
 */
public class CartItems {

    // Use a Map for efficient lookup by VariantId
    private final Map<VariantId, CartItem> items = new LinkedHashMap<>(); // Keep insertion order
    private Currency cartCurrency = null;

    /**
     * Default constructor for an empty item collection.
     */
    public CartItems() {}

    /**
     * Constructor to initialize with a list of items.
     * Ensures all items have the same currency.
     * @param initialItems List of items to start with.
     * @throws IllegalArgumentException if items have mixed currencies.
     */
    public CartItems(List<CartItem> initialItems) {
        if (initialItems != null && !initialItems.isEmpty()) {
            // Set currency from the first item
            cartCurrency = initialItems.get(0).getVariant().getPrice().getCurrency();
            for (CartItem item : initialItems) {
                // Validate currency consistency
                if (!cartCurrency.equals(item.getVariant().getPrice().getCurrency())) {
                    throw new IllegalArgumentException("Cannot initialize CartItems with mixed currencies");
                }
                // FIX: Use variant.getId() for the key
                items.put(item.getVariant().getId(), item);
            }
        }
    }

    /**
     * @return An unmodifiable view of the cart items as a list.
     */
    public List<CartItem> asList() {
        return Collections.unmodifiableList(new ArrayList<>(items.values()));
    }

    /**
     * Adds a specified quantity of a variant to the cart.
     * If the variant already exists, increases its quantity.
     * Sets the cart currency on the first add.
     *
     * @param variant  The variant to add.
     * @param quantity The positive quantity to add.
     * @throws IllegalArgumentException if quantity <= 0, variant is null, or currency mixes.
     */
    public void add(Variant variant, int quantity) {
        Objects.requireNonNull(variant, "variant cannot be null");
        if (quantity <= 0) throw new IllegalArgumentException("quantity must be > 0");

        // FIX: Changed variant.price().currency() to variant.getPrice().getCurrency()
        Currency variantCurrency = variant.getPrice().getCurrency();
        if (cartCurrency == null) {
            cartCurrency = variantCurrency; // Set currency on first add
        } else if (!cartCurrency.equals(variantCurrency)) {
            // Enforce single currency per cart
            throw new IllegalArgumentException("Cannot mix currencies in cart. Expected " + cartCurrency + ", got " + variantCurrency);
        }

        // FIX: Use variant.getId()
        VariantId variantId = variant.getId();
        CartItem existingItem = items.get(variantId);

        if (existingItem != null) {
            // Item exists, increase quantity
            items.put(variantId, existingItem.increaseQuantity(quantity));
        } else {
            // New item
            items.put(variantId, new CartItem(variant, quantity));
        }
    }

    /**
     * Removes a variant entirely from the cart.
     * Resets cart currency if the cart becomes empty.
     *
     * @param variant The variant to remove.
     */
    public void remove(Variant variant) {
        Objects.requireNonNull(variant, "variant cannot be null");
        // FIX: Use variant.getId()
        if (items.remove(variant.getId()) != null) {
            // Reset currency only if the remove was successful and cart is now empty
            if (items.isEmpty()) {
                cartCurrency = null;
            }
        }
    }

    /**
     * Updates the quantity of a variant in the cart.
     * Adds the item if it doesn't exist and newQuantity > 0.
     * Removes the item if newQuantity is 0.
     * Resets cart currency if the cart becomes empty.
     *
     * @param variant     The variant to update.
     * @param newQuantity The non-negative target quantity.
     * @throws IllegalArgumentException if newQuantity < 0 or variant is null.
     */
    public void updateQuantity(Variant variant, int newQuantity) {
        Objects.requireNonNull(variant, "variant cannot be null");
        if (newQuantity < 0) throw new IllegalArgumentException("newQuantity must be >= 0");

        // FIX: Use variant.getId()
        VariantId variantId = variant.getId();

        if (newQuantity == 0) {
            // Remove item if quantity is zero
            if (items.remove(variantId) != null && items.isEmpty()) {
                cartCurrency = null; // Reset currency if cart becomes empty
            }
        } else {
            // Add or update item
            if (items.containsKey(variantId)) {
                // Update existing item
                items.put(variantId, new CartItem(variant, newQuantity));
            } else {
                // Add new item (delegates currency check to add method)
                add(variant, newQuantity);
            }
        }
    }

    /**
     * @return true if the cart contains no items.
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * Removes all items from the cart and resets the currency.
     */
    public void clear() {
        items.clear();
        cartCurrency = null;
    }

    /**
     * Checks if a specific variant is present in the cart.
     * @param variant The variant to check for.
     * @return true if the variant is in the cart.
     */
    public boolean hasItem(Variant variant) {
        // FIX: Use variant.getId()
        return variant != null && items.containsKey(variant.getId());
    }

    /**
     * Gets the current quantity of a specific variant in the cart.
     * @param variant The variant to query.
     * @return The quantity, or 0 if the variant is not in the cart.
     */
    public int getQuantity(Variant variant) {
        // FIX: Use variant.getId()
        CartItem item = variant != null ? items.get(variant.getId()) : null;
        return item != null ? item.getQuantity() : 0;
    }

    /**
     * @return The currency associated with this cart, or null if empty.
     */
    public Currency getCartCurrency() {
        return cartCurrency;
    }
}
