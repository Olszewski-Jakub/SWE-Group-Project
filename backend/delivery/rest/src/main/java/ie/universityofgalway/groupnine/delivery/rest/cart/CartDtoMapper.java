package ie.universityofgalway.groupnine.delivery.rest.cart;

import ie.universityofgalway.groupnine.delivery.rest.cart.dto.CartResponse;
import ie.universityofgalway.groupnine.delivery.rest.cart.dto.UpdateCartItemRequest;
import ie.universityofgalway.groupnine.domain.cart.ShoppingCart;
import java.util.List;

/**
 * Mapper to convert domain ShoppingCart → REST DTO.
 */
public final class CartDtoMapper {

    private CartDtoMapper() {}

    public static CartResponse toDto(ShoppingCart cart) {
        List<UpdateCartItemRequest> items = cart.items().asList().stream()
                .map(i -> new UpdateCartItemRequest(
                        i.variant().id().id(), // just the UUID
                        i.quantity()
                ))
                .toList();

        return new CartResponse(
                cart.id().id().toString(),
                cart.status().name(),
                cart.createdAt().toEpochMilli(),
                cart.updatedAt().toEpochMilli(),
                items
        );
    }
}

