package ie.universityofgalway.groupnine.delivery.rest.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * DTO for adding an item to a cart.
 */
public class AddCartItemRequest {

    @NotNull(message = "variantId is required")
    private UUID variantId;

    @Min(value = 1, message = "quantity must be at least 1")
    private int quantity;

    public AddCartItemRequest() {
    }

    public AddCartItemRequest(UUID variantId, int quantity) {
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
