package ie.universityofgalway.groupnine.service.cart.usecase;

import ie.universityofgalway.groupnine.domain.cart.CartId;
import ie.universityofgalway.groupnine.domain.cart.ShoppingCart;
import ie.universityofgalway.groupnine.domain.product.Variant;
import ie.universityofgalway.groupnine.domain.product.VariantId;
import ie.universityofgalway.groupnine.service.cart.CartNotFoundException;
import ie.universityofgalway.groupnine.service.cart.ShoppingCartPort;
import ie.universityofgalway.groupnine.service.product.VariantNotFoundException;
import ie.universityofgalway.groupnine.service.product.VariantPort;
import org.springframework.stereotype.Service;

/**
 * Use case for adding an item to a shopping cart.
 */
@Service
public class AddCartItemUseCase {
    private final ShoppingCartPort carts;
    private final VariantPort variants;

    public AddCartItemUseCase(ShoppingCartPort carts, VariantPort variants) {
        this.carts = carts;
        this.variants = variants;
    }

    /**
     * Adds an item to the given cart.
     *
     * @param cartId    the {@link CartId} of the cart
     * @param variantId the {@link VariantId} of the product variant
     * @param quantity  the quantity to add
     * @return the updated {@link ShoppingCart}
     * @throws IllegalArgumentException   if quantity is not positive
     * @throws CartNotFoundException      if the cart does not exist
     * @throws VariantNotFoundException   if the variant does not exist
     */
    public ShoppingCart execute(CartId cartId, VariantId variantId, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");

        ShoppingCart cart = carts.findById(cartId)
                .orElseThrow(() -> new CartNotFoundException(cartId.toString()));

        Variant variant = variants.findById(variantId)
                .orElseThrow(() -> new VariantNotFoundException(variantId.toString()));

        cart.addItem(variant, quantity);
        return carts.save(cart);
    }
}
