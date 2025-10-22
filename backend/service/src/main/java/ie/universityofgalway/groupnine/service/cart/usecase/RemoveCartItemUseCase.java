package ie.universityofgalway.groupnine.service.cart.usecase;

import ie.universityofgalway.groupnine.domain.cart.ShoppingCart;
import ie.universityofgalway.groupnine.domain.product.Variant;
import ie.universityofgalway.groupnine.domain.product.VariantId;
import ie.universityofgalway.groupnine.service.cart.ShoppingCartPort;
import ie.universityofgalway.groupnine.service.product.VariantPort;

import java.util.Objects;
import java.util.UUID;

public class RemoveCartItemUseCase {
    private final ShoppingCartPort carts;
    private final VariantPort variants;

    public RemoveCartItemUseCase(ShoppingCartPort carts, VariantPort variants) {
        this.carts = carts;
        this.variants = variants;
    }

    public ShoppingCart execute(String cartIdRaw, UUID variantId) {
        Objects.requireNonNull(cartIdRaw, "cartId");
        UUID cartUuid = UUID.fromString(cartIdRaw);
        ShoppingCart cart = carts.findById(new ie.universityofgalway.groupnine.domain.cart.CartId(cartUuid))
                .orElseThrow(() -> new IllegalArgumentException("Cart not found: " + cartIdRaw));

        Variant variant = variants.findById(new VariantId(variantId))
                .orElseThrow(() -> new IllegalArgumentException("Variant not found: " + variantId));

        cart.removeItem(variant);
        carts.save(cart);
        return cart;
    }
}

