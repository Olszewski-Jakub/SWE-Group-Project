package ie.universityofgalway.groupnine.delivery.rest.cart;

import ie.universityofgalway.groupnine.delivery.rest.cart.dto.CartResponse;
import ie.universityofgalway.groupnine.delivery.rest.cart.dto.UpdateCartItemRequest;
import ie.universityofgalway.groupnine.domain.cart.ShoppingCart;
import java.util.List;

/**
 * Utility class for mapping ShoppingCart domain objects to CartResponse DTOs.
 */
public final class CartDtoMapper {

    private CartDtoMapper() {}

    /**
     * Converts a ShoppingCart domain object to a CartResponse DTO.
     *
     * @param cart The ShoppingCart to convert.
     * @return The corresponding CartResponse DTO.
     */
    public static CartResponse toDto(ShoppingCart cart) {
        List<UpdateCartItemRequest> items = cart.getItems().stream()
                .map(i -> new UpdateCartItemRequest(
                        i.getVariant().getId().getId(),
                        i.getQuantity()
                ))
                .toList();

        return new CartResponse(
                cart.getId().getId().toString(),
                cart.getStatus().name(),
                cart.getCreatedAt().toEpochMilli(),
                cart.getUpdatedAt().toEpochMilli(),
                items
        );
    }
}