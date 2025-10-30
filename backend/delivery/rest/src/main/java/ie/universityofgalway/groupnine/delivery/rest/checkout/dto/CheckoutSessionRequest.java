package ie.universityofgalway.groupnine.delivery.rest.checkout.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request payload used by {@code POST /api/v1/checkout/sessions} to start a hosted Checkout.
 * Only the cart identifier is required; authentication is derived from the access token.
 */
public final class CheckoutSessionRequest {
    @NotNull
    public UUID cartId;

    public CheckoutSessionRequest() {}
    public CheckoutSessionRequest(UUID cartId) { this.cartId = cartId; }
}
