package ie.universityofgalway.groupnine.service.cart.usecase;

import ie.universityofgalway.groupnine.domain.cart.CartId;
import ie.universityofgalway.groupnine.domain.cart.ShoppingCart;
import ie.universityofgalway.groupnine.domain.product.Variant;
import ie.universityofgalway.groupnine.domain.product.VariantId;
import ie.universityofgalway.groupnine.domain.cart.exception.CartNotFoundException;
import ie.universityofgalway.groupnine.service.cart.port.ShoppingCartPort;
import ie.universityofgalway.groupnine.domain.product.exception.VariantNotFoundException;
import ie.universityofgalway.groupnine.service.product.port.VariantPort;
import org.springframework.stereotype.Service;

/**
 * A service use case responsible for updating the quantity of an item within a shopping cart.
 */
@Service
public class UpdateCartItemUseCase {

    private final ShoppingCartPort cartPort;
    private final VariantPort variantPort;
    private final RemoveCartItemUseCase removeCartItemUseCase;

    /**
     * Constructs a new UpdateCartItemUseCase with the required dependencies.
     *
     * @param cartPort The port for accessing shopping cart data.
     * @param variantPort The port for accessing product variant data.
     * @param removeCartItemUseCase The use case for removing items from a cart.
     */
    public UpdateCartItemUseCase(ShoppingCartPort cartPort, VariantPort variantPort, RemoveCartItemUseCase removeCartItemUseCase) {
        this.cartPort = cartPort;
        this.variantPort = variantPort;
        this.removeCartItemUseCase = removeCartItemUseCase;
    }

    /**
     * Executes the logic to update a variant's quantity in a specified cart.
     * If the target quantity is zero, this operation delegates to the {@link RemoveCartItemUseCase}.
     *
     * @param cartId The {@link CartId} of the cart to be modified.
     * @param variantId The {@link VariantId} of the product variant to update.
     * @param quantity The new target quantity for the item, which must be non-negative.
     * @return The updated {@link ShoppingCart} instance after the modification.
     * @throws IllegalArgumentException if the provided quantity is negative.
     * @throws CartNotFoundException if no cart with the given ID exists.
     * @throws VariantNotFoundException if no variant with the given ID exists.
     */
    public ShoppingCart execute(CartId cartId, VariantId variantId, int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }

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