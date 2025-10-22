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
 * Use case for removing an item from a shopping cart.
 */
@Service
public class RemoveCartItemUseCase {
    private final ShoppingCartPort carts;
    private final VariantPort variants;

    public RemoveCartItemUseCase(ShoppingCartPort carts, VariantPort variants) {
        this.carts = carts;
        this.variants = variants;
    }

    public ShoppingCart execute(CartId cartId, VariantId variantId) {
        ShoppingCart cart = carts.findById(cartId)
                .orElseThrow(() -> new CartNotFoundException(cartId.toString()));

        Variant variant = variants.findById(variantId)
                .orElseThrow(() -> new VariantNotFoundException(variantId.toString()));

        cart.removeItem(variant);
        return carts.save(cart);
    }
}
