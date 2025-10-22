package ie.universityofgalway.groupnine.delivery.rest.cart.dto;

import jakarta.validation.constraints.Min;

/**
 * DTO for updating an item's quantity.
 */
public class UpdateCartItemQuantityRequest {

    @Min(0)
    private int quantity;

    public UpdateCartItemQuantityRequest() {
    }

    public UpdateCartItemQuantityRequest(int quantity) {
        this.quantity = quantity;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}

