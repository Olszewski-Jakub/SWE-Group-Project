package ie.universityofgalway.groupnine.delivery.rest.cart;

import ie.universityofgalway.groupnine.delivery.rest.cart.dto.CartResponse;
import ie.universityofgalway.groupnine.delivery.rest.cart.dto.UpdateCartItemRequest;
import ie.universityofgalway.groupnine.domain.cart.CartItem; // Import CartItem
import ie.universityofgalway.groupnine.domain.cart.ShoppingCart;

import java.util.List;
// Removed unused UUID import

/**
 * Utility class for mapping ShoppingCart domain objects to CartResponse DTOs.
 */
public final class CartDtoMapper {

    private CartDtoMapper() {}

    /**
     * Converts a ShoppingCart domain object to a CartResponse DTO.
     * Updated to use getter methods.
     *
     * @param cart the ShoppingCart to convert
     * @return the corresponding CartResponse DTO
     */
    public static CartResponse toDto(ShoppingCart cart) {
        // FIX: Use cart.getItems() which returns List<CartItem>
        List<UpdateCartItemRequest> items = cart.getItems().stream()
                .map(i -> new UpdateCartItemRequest(
                        // FIX: Use i.getVariant().getId().getId()
                        i.getVariant().getId().getId(), // Get UUID from VariantId
                        i.getQuantity()
                ))
                .toList();

        return new CartResponse(
                // FIX: Use cart.getId().getId() to get the UUID, then convert to String
                cart.getId().getId().toString(),
                // FIX: Use cart.getStatus()
                cart.getStatus().name(),
                // FIX: Use cart.getCreatedAt() and cart.getUpdatedAt()
                cart.getCreatedAt().toEpochMilli(),
                cart.getUpdatedAt().toEpochMilli(),
                items
        );
    }
}

