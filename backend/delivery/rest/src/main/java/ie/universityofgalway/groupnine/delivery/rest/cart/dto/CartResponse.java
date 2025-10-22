package ie.universityofgalway.groupnine.delivery.rest.cart.dto;

import ie.universityofgalway.groupnine.delivery.rest.cart.dto.UpdateCartItemRequest;
import java.util.List;

/**
 * Response DTO representing a shopping cart.
 */
public record CartResponse(
        String id,
        String status,
        long createdAt,
        long updatedAt,
        List<UpdateCartItemRequest> items
) {}
