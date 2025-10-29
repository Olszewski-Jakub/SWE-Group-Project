package ie.universityofgalway.groupnine.service.product.admin.usecase;

import ie.universityofgalway.groupnine.domain.product.*;
import ie.universityofgalway.groupnine.service.product.port.ProductPort;

import java.util.UUID;

public class CreateProductUseCase {
    private final ProductPort port;

    public CreateProductUseCase(ProductPort port) { this.port = port; }

    public Product execute(Product product) {
        if (product == null) throw new IllegalArgumentException("product required");
        validateProductBasics(product.getName(), product.getCategory(), product.getStatus());

        UUID id = product.getId() != null && product.getId().getId() != null ? product.getId().getId() : UUID.randomUUID();
        if (product.getId() != null && product.getId().getId() != null && port.productExistsByUuid(product.getId().getId())) {
            // idempotent create
            return port.findById(new ProductId(product.getId().getId())).orElseThrow();
        }

        // Normalize/generate variant ids when missing and validate
        java.util.List<Variant> normalizedVariants = java.util.Collections.emptyList();
        if (product.getVariants() != null) {
            normalizedVariants = product.getVariants().stream().map(v -> {
                VariantId vid = (v.getId() == null || v.getId().getId() == null) ? new VariantId(UUID.randomUUID()) : v.getId();
                Variant nv = new Variant(vid, v.getSku(), v.getPrice(), v.getStock(), v.getAttributes());
                validateVariant(nv);
                return nv;
            }).toList();
        }

        Product toSave = new Product(
                new ProductId(id),
                product.getName().trim(),
                product.getDescription(),
                product.getCategory().trim(),
                product.getStatus(),
                normalizedVariants,
                java.time.Instant.now(),
                java.time.Instant.now()
        );
        return port.saveProduct(toSave);
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

    private void validateVariant(Variant v) {
        if (v == null) throw new IllegalArgumentException("variant required");
        if (v.getSku() == null || v.getSku().getValue() == null || v.getSku().getValue().isBlank()) {
            throw new IllegalArgumentException("sku must not be blank");
        }
        if (v.getPrice() == null || v.getPrice().getAmount() == null || v.getPrice().getAmount().signum() < 0) {
            throw new IllegalArgumentException("amount must be >= 0");
        }
        if (v.getPrice().getCurrency() == null) {
            throw new IllegalArgumentException("currency is required");
        }
        if (v.getStock() == null || v.getStock().getQuantity() < 0 || v.getStock().getReserved() < 0) {
            throw new IllegalArgumentException("stock must be >= 0");
        }
        if (v.getStock().getReserved() > v.getStock().getQuantity()) {
            throw new IllegalArgumentException("reserved cannot exceed quantity");
        }
    }
}
