package ie.universityofgalway.groupnine.delivery.rest.cart;

import ie.universityofgalway.groupnine.delivery.rest.cart.dto.CartResponse;
import ie.universityofgalway.groupnine.delivery.rest.cart.dto.CartResponseItem;
import ie.universityofgalway.groupnine.domain.cart.ShoppingCart;
import ie.universityofgalway.groupnine.domain.product.Product;
import ie.universityofgalway.groupnine.domain.product.VariantId;
import ie.universityofgalway.groupnine.service.product.port.ProductPort;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Maps ShoppingCart domain objects to CartResponse DTOs, enriching items with
 * product information based on the variant ID.
 */
@Component
public class CartDtoMapper {

    private final ProductPort productPort;

    public CartDtoMapper(ProductPort productPort) {
        this.productPort = Objects.requireNonNull(productPort);
    }

    /**
     * Converts a ShoppingCart domain object to a CartResponse DTO.
     * For each item, looks up the parent product via variant ID and includes
     * product name/description.
     */
    public CartResponse toDto(ShoppingCart cart) {
        List<CartResponseItem> items = cart.getItems().stream().map(i -> {
            var variantId = i.getVariant().getId();
            Optional<Product> prod = productPort.findByVariantId(new VariantId(variantId.getId()));
            var price = i.getVariant().getPrice();
            var priceDto = new ie.universityofgalway.groupnine.delivery.rest.product.dto.MoneyDto(
                price.getAmount(), price.getCurrency().getCurrencyCode()
            );
            var sub = i.subtotal();
            var subtotalDto = new ie.universityofgalway.groupnine.delivery.rest.product.dto.MoneyDto(
                sub.getAmount(), sub.getCurrency().getCurrencyCode()
            );
            return new CartResponseItem(
                variantId.getId(),
                i.getQuantity(),
                prod.map(p -> p.getId().getId()).orElse(null),
                prod.map(Product::getName).orElse(null),
                prod.map(Product::getDescription).orElse(null),
                priceDto,
                subtotalDto
            );
        }).toList();

        var total = cart.total();
        var totalDto = new ie.universityofgalway.groupnine.delivery.rest.product.dto.MoneyDto(
            total.getAmount(), total.getCurrency().getCurrencyCode()
        );
        return new CartResponse(
            cart.getId().getId().toString(),
            cart.getStatus().name(),
            cart.getCreatedAt().toEpochMilli(),
            cart.getUpdatedAt().toEpochMilli(),
            items,
            totalDto
        );
    }
}
