package ie.universityofgalway.groupnine.delivery.rest.cart;

import ie.universityofgalway.groupnine.delivery.rest.cart.dto.CartResponse;
import ie.universityofgalway.groupnine.delivery.rest.cart.dto.UpdateCartItemRequest;
import ie.universityofgalway.groupnine.domain.cart.ShoppingCart;

import java.util.List;
import java.util.UUID;

/**
 * Utility class for mapping ShoppingCart domain objects to CartResponse DTOs.
 */
public final class CartDtoMapper {

    private CartDtoMapper() {}

    /**
     * Converts a ShoppingCart domain object to a CartResponse DTO.
     *
     * @param cart the ShoppingCart to convert
     * @return the corresponding CartResponse DTO
     */
    public static CartResponse toDto(ShoppingCart cart) {
        List<UpdateCartItemRequest> items = cart.items().asList().stream()
                .map(i -> new UpdateCartItemRequest(
                        i.getVariant().id().id(), // UUID
                        i.getQuantity()
                ))
                .toList();

        return new CartResponse(
                cart.id().getId().toString(), // convert UUID to String for CartResponse
                cart.status().name(),
                cart.createdAt().toEpochMilli(),
                cart.updatedAt().toEpochMilli(),
                items
        );
    }
}
