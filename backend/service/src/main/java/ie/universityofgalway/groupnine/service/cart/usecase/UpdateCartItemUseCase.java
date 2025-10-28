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
 * Use case for updating the quantity of an item in a shopping cart.
 */
@Service
public class UpdateCartItemUseCase {

    private final ShoppingCartPort cartPort;
    private final VariantPort variantPort;
    private final RemoveCartItemUseCase removeCartItemUseCase;

    public UpdateCartItemUseCase(ShoppingCartPort cartPort, VariantPort variantPort, RemoveCartItemUseCase removeCartItemUseCase) {
        this.cartPort = cartPort;
        this.variantPort = variantPort;
        this.removeCartItemUseCase = removeCartItemUseCase;
    }

    /**
     * Updates the quantity of a variant in the cart.
     * If the quantity is zero, delegates removal to {@link RemoveCartItemUseCase}.
     *
     * @param cartId    the {@link CartId} of the cart
     * @param variantId the {@link VariantId} of the product variant
     * @param quantity  the new quantity (must be >= 0)
     * @return the updated {@link ShoppingCart}
     * @throws IllegalArgumentException if quantity is negative
     * @throws CartNotFoundException if the cart does not exist
     * @throws VariantNotFoundException if the variant does not exist
     */
    public ShoppingCart execute(CartId cartId, VariantId variantId, int quantity) {
        if (quantity < 0) throw new IllegalArgumentException("Quantity cannot be negative");

        if (quantity == 0) {
            return removeCartItemUseCase.execute(cartId, variantId);
        }

        ShoppingCart cart = cartPort.findById(cartId)
                .orElseThrow(() -> new CartNotFoundException(cartId.toString()));

        Variant variant = variantPort.findById(variantId)
                .orElseThrow(() -> new VariantNotFoundException(variantId.toString()));

        cart.updateItemQuantity(variant, quantity);
        return cartPort.save(cart);
    }
}