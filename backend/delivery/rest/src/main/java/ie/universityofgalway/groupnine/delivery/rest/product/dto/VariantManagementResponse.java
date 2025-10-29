package ie.universityofgalway.groupnine.delivery.rest.product.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class VariantManagementResponse {
    private UUID id;
    private String sku;
    private MoneyDto price;
    private StockDto stock;
    private String imageUrl;
    private List<AttributeDto> attributes;
    private Instant createdAt;
    private Instant updatedAt;

    public VariantManagementResponse() {}

    public VariantManagementResponse(UUID id, String sku, MoneyDto price, StockDto stock, String imageUrl, List<AttributeDto> attributes, Instant createdAt, Instant updatedAt) {
        this.id = id; this.sku = sku; this.price = price; this.stock = stock; this.imageUrl = imageUrl; this.attributes = attributes; this.createdAt = createdAt; this.updatedAt = updatedAt;
    }
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public MoneyDto getPrice() { return price; }
    public void setPrice(MoneyDto price) { this.price = price; }
    public StockDto getStock() { return stock; }
    public void setStock(StockDto stock) { this.stock = stock; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public List<AttributeDto> getAttributes() { return attributes; }
    public void setAttributes(List<AttributeDto> attributes) { this.attributes = attributes; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
