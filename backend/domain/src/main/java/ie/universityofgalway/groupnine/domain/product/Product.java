package ie.universityofgalway.groupnine.domain.product;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate root for products listed in the catalog.
 * Converted from a record to a class.
 */
public final class Product {

    private final ProductId id;
    private final String name;
    private final String description;
    private final String category;
    private final ProductStatus status;
    private final List<Variant> variants;
    private final Instant createdAt;
    private final Instant updatedAt;

    public Product(ProductId id, String name, String description, String category,
                   ProductStatus status, List<Variant> variants,
                   Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.status = status;
        this.variants = List.copyOf(variants); // Ensure immutability
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public ProductId getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public ProductStatus getStatus() { return status; }
    public List<Variant> getVariants() { return List.copyOf(variants); } // Return an immutable view
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(id, product.id) &&
               Objects.equals(name, product.name) &&
               Objects.equals(description, product.description) &&
               Objects.equals(category, product.category) &&
               status == product.status &&
               Objects.equals(variants, product.variants) &&
               Objects.equals(createdAt, product.createdAt) &&
               Objects.equals(updatedAt, product.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, category, status, variants, createdAt, updatedAt);
    }

    @Override
    public String toString() {
        return "Product[" +
               "id=" + id + ", " +
               "name='" + name + '\'' + ", " +
               "description='" + description + '\'' + ", " +
               "category='" + category + '\'' + ", " +
               "status=" + status + ", " +
               "variants=" + variants + ", " +
               "createdAt=" + createdAt + ", " +
               "updatedAt=" + updatedAt +
               ']';
    }
}
