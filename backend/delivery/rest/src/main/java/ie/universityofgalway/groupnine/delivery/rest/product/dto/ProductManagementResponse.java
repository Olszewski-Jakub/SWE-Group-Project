package ie.universityofgalway.groupnine.delivery.rest.product.dto;

import ie.universityofgalway.groupnine.domain.product.ProductStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class ProductManagementResponse {
    private UUID id;
    private String name;
    private String description;
    private String category;
    private ProductStatus status;
    private List<VariantManagementResponse> variants;
    private Instant createdAt;
    private Instant updatedAt;

    public ProductManagementResponse() {}

    public ProductManagementResponse(UUID id, String name, String description, String category, ProductStatus status, List<VariantManagementResponse> variants, Instant createdAt, Instant updatedAt) {
        this.id = id; this.name = name; this.description = description; this.category = category; this.status = status; this.variants = variants; this.createdAt = createdAt; this.updatedAt = updatedAt;
    }
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public ProductStatus getStatus() { return status; }
    public void setStatus(ProductStatus status) { this.status = status; }
    public List<VariantManagementResponse> getVariants() { return variants; }
    public void setVariants(List<VariantManagementResponse> variants) { this.variants = variants; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
