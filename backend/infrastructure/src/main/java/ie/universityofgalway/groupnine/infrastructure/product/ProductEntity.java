package ie.universityofgalway.groupnine.infrastructure.product;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a product in the persistence layer.
 * This entity is mapped to the "products" table in the database.
 */
@Entity
@Table(name = "products")
public class ProductEntity {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * The public, unique, and immutable identifier for the product.
   * Used for external communication and domain mapping.
   */
  @Column(unique = true, nullable = false, updatable = false)
  private UUID uuid = UUID.randomUUID(); 

  @Column(nullable=false, length=120) private String name;
  @Column(columnDefinition="text")     private String description;
  @Column(nullable=false, length=60)    private String category;
  @Column(name="created_at", nullable=false)   private Instant createdAt = Instant.now();
  @Column(name="updated_at", nullable=false)   private Instant updatedAt = Instant.now();

  /**
   * A list of all variants associated with this product.
   * The relationship is managed by the ProductEntity, so changes
   * (additions/removals) to this list are cascaded to the database.
   */
  @OneToMany(
      mappedBy = "product",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.EAGER 
  )
  private List<VariantEntity> variants = new ArrayList<>();

  /**
   * Updates the updatedAt timestamp before a database update.
   */
  @PreUpdate public void touch() { this.updatedAt = Instant.now(); }

  // Getters and Setters...
  public Long getId() { return id; }
  public UUID getUuid() { return uuid; }
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }
  public String getCategory() { return category; }
  public void setCategory(String category) { this.category = category; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
  public List<VariantEntity> getVariants() { return variants; }
  public void setVariants(List<VariantEntity> variants) { this.variants = variants; }
}