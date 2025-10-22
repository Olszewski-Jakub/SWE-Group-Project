package ie.universityofgalway.groupnine.delivery.rest.cart.dto;

import java.util.List;

/**
 * Response DTO representing a shopping cart.
 */
public class CartResponse {

    private String id;
    private String status;
    private long createdAt;
    private long updatedAt;
    private List<UpdateCartItemRequest> items;

    public CartResponse() {
    }

    public CartResponse(String id, String status, long createdAt, long updatedAt, List<UpdateCartItemRequest> items) {
        this.id = id;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.items = items;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<UpdateCartItemRequest> getItems() {
        return items;
    }

    public void setItems(List<UpdateCartItemRequest> items) {
        this.items = items;
    }
}