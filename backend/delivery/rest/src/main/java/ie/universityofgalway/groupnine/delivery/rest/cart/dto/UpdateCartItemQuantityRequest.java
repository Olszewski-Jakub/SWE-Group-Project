package ie.universityofgalway.groupnine.delivery.rest.cart.dto;

import jakarta.validation.constraints.Min;

public record UpdateCartItemQuantityRequest(
        @Min(0) int quantity
) {}

