package ie.universityofgalway.groupnine.service.product.admin;

import ie.universityofgalway.groupnine.domain.product.ProductId;
import ie.universityofgalway.groupnine.domain.product.ProductStatus;

public class UpdateProductCommand {
    private final ProductId id;
    private final String name;
    private final String description;
    private final String category;
    private final ProductStatus status;

    public UpdateProductCommand(ProductId id, String name, String description, String category, ProductStatus status) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.status = status;
    }

    public ProductId getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public ProductStatus getStatus() { return status; }
}
