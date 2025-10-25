package ie.universityofgalway.groupnine.delivery.rest.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * DTO for reading/updating cart items.
 * Used inside CartResponse.
 */
public record UpdateCartItemRequest(
        @NotNull UUID variantId,

        @Min(0) int quantity
) {}
