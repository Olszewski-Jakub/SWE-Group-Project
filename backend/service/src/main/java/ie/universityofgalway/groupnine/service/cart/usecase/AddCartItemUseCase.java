package ie.universityofgalway.groupnine.service.cart.usecase;

import ie.universityofgalway.groupnine.domain.cart.ShoppingCart;
import ie.universityofgalway.groupnine.domain.product.Variant;
import ie.universityofgalway.groupnine.service.cart.ShoppingCartPort;
import ie.universityofgalway.groupnine.service.product.VariantPort;

import java.util.Objects;
import java.util.UUID;

public class AddCartItemUseCase {
    private final ShoppingCartPort carts;
    private final VariantPort variants;

    public AddCartItemUseCase(ShoppingCartPort carts, VariantPort variants) {
        this.carts = carts;
        this.variants = variants;
    }

    public ShoppingCart execute(String cartIdRaw, UUID variantId, int quantity) {
        Objects.requireNonNull(cartIdRaw, "cartId");
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");

        UUID cartUuid = UUID.fromString(cartIdRaw);
        ShoppingCart cart = carts.findById(new ie.universityofgalway.groupnine.domain.cart.CartId(cartUuid))
                .orElseThrow(() -> new IllegalArgumentException("Cart not found: " + cartIdRaw));

        Variant variant = variants.findById(new ie.universityofgalway.groupnine.domain.product.VariantId(variantId))
                .orElseThrow(() -> new IllegalArgumentException("Variant not found: " + variantId));

        cart.addItem(variant, quantity);
        carts.save(cart);
        return cart;
    }
}

