package ie.universityofgalway.groupnine.delivery.rest.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * DTO for adding an item to a cart.
 * Validates that variantId is provided and quantity >= 1.
 */
public record AddCartItemRequest(
        @NotNull(message = "variantId is required")
        UUID variantId,

        @Min(value = 1, message = "quantity must be at least 1")
        int quantity
) {}

