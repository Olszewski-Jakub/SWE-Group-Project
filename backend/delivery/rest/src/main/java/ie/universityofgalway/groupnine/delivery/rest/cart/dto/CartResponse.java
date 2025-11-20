package ie.universityofgalway.groupnine.delivery.rest.cart.dto;

import java.util.List;

/**
 * Response DTO representing a shopping cart enriched with product details for items.
 */
public class CartResponse {

    private String id;
    private String status;
    private long createdAt;
    private long updatedAt;
    private List<CartResponseItem> items;
    private ie.universityofgalway.groupnine.delivery.rest.product.dto.MoneyDto totalPrice;

    public CartResponse() {}

    public CartResponse(String id, String status, long createdAt, long updatedAt, List<CartResponseItem> items,
                        ie.universityofgalway.groupnine.delivery.rest.product.dto.MoneyDto totalPrice) {
        this.id = id;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.items = items;
        this.totalPrice = totalPrice;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    public List<CartResponseItem> getItems() { return items; }
    public void setItems(List<CartResponseItem> items) { this.items = items; }

    public ie.universityofgalway.groupnine.delivery.rest.product.dto.MoneyDto getTotalPrice() { return totalPrice; }
    public void setTotalPrice(ie.universityofgalway.groupnine.delivery.rest.product.dto.MoneyDto totalPrice) { this.totalPrice = totalPrice; }
}
