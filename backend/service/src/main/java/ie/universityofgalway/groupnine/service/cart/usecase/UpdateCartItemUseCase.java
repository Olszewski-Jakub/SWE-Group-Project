package ie.universityofgalway.groupnine.service.cart.usecase;

import ie.universityofgalway.groupnine.domain.cart.ShoppingCart;
import ie.universityofgalway.groupnine.domain.product.Variant;
import ie.universityofgalway.groupnine.domain.product.VariantId;
import ie.universityofgalway.groupnine.service.cart.ShoppingCartPort;
import ie.universityofgalway.groupnine.service.product.VariantPort;

import java.util.UUID;

public class UpdateCartItemUseCase {
    private final ShoppingCartPort cartPort;
    private final VariantPort variantPort;
    private final RemoveCartItemUseCase removeCartItemUseCase;

    public UpdateCartItemUseCase(ShoppingCartPort cartPort, VariantPort variantPort, RemoveCartItemUseCase removeCartItemUseCase) {
        this.cartPort = cartPort;
        this.variantPort = variantPort;
        this.removeCartItemUseCase = removeCartItemUseCase;
    }

    public ShoppingCart execute(String cartId, UUID variantId, int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }

        if (quantity == 0) {
            // Delegate removal to RemoveCartItemUseCase
            return removeCartItemUseCase.execute(cartId, variantId);
        }

        ShoppingCart cart = cartPort.findById(new ie.universityofgalway.groupnine.domain.cart.CartId(cartId))
                .orElseThrow(() -> new IllegalArgumentException("Cart not found: " + cartId));

        Variant variant = variantPort.findById(new VariantId(variantId))
                .orElseThrow(() -> new IllegalArgumentException("Variant not found: " + variantId));

        cart.updateItemQuantity(variant, quantity);
        return cartPort.save(cart);
    }
}

