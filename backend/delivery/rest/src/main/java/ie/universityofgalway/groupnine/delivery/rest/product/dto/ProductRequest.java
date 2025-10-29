package ie.universityofgalway.groupnine.delivery.rest.product.dto;

import ie.universityofgalway.groupnine.domain.product.ProductStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class ProductRequest {
    @Size(max=120) @NotBlank private String name;
    @Size(max=10000) private String description;
    @Size(max=60) @NotBlank private String category;
    @NotNull private ProductStatus status;
    @Valid private List<VariantRequest> variants;

    public ProductRequest() {}

    public ProductRequest(String name, String description, String category, ProductStatus status, List<VariantRequest> variants) {
        this.name = name; this.description = description; this.category = category; this.status = status; this.variants = variants;
    }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public ProductStatus getStatus() { return status; }
    public void setStatus(ProductStatus status) { this.status = status; }
    public List<VariantRequest> getVariants() { return variants; }
    public void setVariants(List<VariantRequest> variants) { this.variants = variants; }
}
