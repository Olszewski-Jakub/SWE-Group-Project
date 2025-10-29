package ie.universityofgalway.groupnine.delivery.rest.product.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public class VariantRequest {
    @NotBlank private String sku;
    @NotNull @Valid private MoneyDto price;
    @NotNull @Valid private StockDto stock;
    @Size(max=255) private String imageUrl;
    @Valid private List<AttributeDto> attributes;
    private UUID id; // optional

    public VariantRequest() {}

    public VariantRequest(String sku, MoneyDto price, StockDto stock, String imageUrl, List<AttributeDto> attributes, UUID id) {
        this.sku = sku; this.price = price; this.stock = stock; this.imageUrl = imageUrl; this.attributes = attributes; this.id = id;
    }

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
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
}
