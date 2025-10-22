package ie.universityofgalway.groupnine.delivery.rest.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * DTO for reading/updating cart items.
 * Used inside CartResponse.
 */
public class UpdateCartItemRequest {

    @NotNull
    private UUID variantId;

    @Min(0)
    private int quantity;

    public UpdateCartItemRequest() {
    }

    public UpdateCartItemRequest(UUID variantId, int quantity) {
        this.variantId = variantId;
        this.quantity = quantity;
    }

    public UUID getVariantId() {
        return variantId;
    }

    public void setVariantId(UUID variantId) {
        this.variantId = variantId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
