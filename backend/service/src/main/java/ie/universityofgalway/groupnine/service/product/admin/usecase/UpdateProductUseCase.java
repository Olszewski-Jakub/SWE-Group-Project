package ie.universityofgalway.groupnine.service.product.admin.usecase;

import ie.universityofgalway.groupnine.domain.product.*;
import ie.universityofgalway.groupnine.service.product.port.ProductPort;
import ie.universityofgalway.groupnine.service.product.admin.InvalidStatusTransitionException;
import ie.universityofgalway.groupnine.service.product.admin.UpdateProductCommand;

public class UpdateProductUseCase {
    private final ProductPort port;

    public UpdateProductUseCase(ProductPort port) { this.port = port; }

    public Product execute(UpdateProductCommand cmd) {
        Product existing = port.findById(cmd.getId()).orElseThrow(() -> new java.util.NoSuchElementException("Product not found"));
        ProductStatus newStatus = cmd.getStatus() == null ? existing.getStatus() : cmd.getStatus();
        validateStatusTransition(existing.getStatus(), newStatus);
        String name = cmd.getName() != null ? cmd.getName().trim() : existing.getName();
        String category = cmd.getCategory() != null ? cmd.getCategory().trim() : existing.getCategory();
        String description = cmd.getDescription() != null ? cmd.getDescription() : existing.getDescription();
        validateProductBasics(name, category, newStatus);

        Product updated = new Product(
                existing.getId(),
                name,
                description,
                category,
                newStatus,
                existing.getVariants(),
                existing.getCreatedAt(),
                java.time.Instant.now()
        );
        return port.saveProduct(updated);
    }

    private void validateProductBasics(String name, String category, ProductStatus status) {
        if (name == null || name.isBlank() || name.length() > 120) {
            throw new IllegalArgumentException("Invalid product name");
        }
        if (category == null || category.isBlank() || category.length() > 60) {
            throw new IllegalArgumentException("Invalid category");
        }
        if (status == null) throw new IllegalArgumentException("status is required");
    }

    private void validateStatusTransition(ProductStatus from, ProductStatus to) {
        if (from == to) return;
        if (from == ProductStatus.ARCHIVED && to == ProductStatus.ACTIVE) {
            throw new InvalidStatusTransitionException(from, to);
        }
    }
}
