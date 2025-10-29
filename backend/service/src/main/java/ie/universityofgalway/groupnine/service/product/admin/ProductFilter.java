package ie.universityofgalway.groupnine.service.product.admin;

import ie.universityofgalway.groupnine.domain.product.ProductStatus;

public class ProductFilter {
    private String category;
    private ProductStatus status;

    public ProductFilter() {}
    public ProductFilter(String category, ProductStatus status) {
        this.category = category;
        this.status = status;
    }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public ProductStatus getStatus() { return status; }
    public void setStatus(ProductStatus status) { this.status = status; }
}
