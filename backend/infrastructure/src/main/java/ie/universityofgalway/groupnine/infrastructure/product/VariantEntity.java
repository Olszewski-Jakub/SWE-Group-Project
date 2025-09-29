package ie.universityofgalway.groupnine.infrastructure.product;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Represents a specific variant of a product (e.g., a certain size or color).
 * Mapped to the "product_variants" table.
 */
@Entity
@Table(name = "product_variants")
public class VariantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The public, unique, and immutable identifier for the variant.
     */
    @Column(unique = true, nullable = false, updatable = false)
    private UUID uuid = UUID.randomUUID();

    /**
     * The parent product to which this variant belongs.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    /**
     * Stock Keeping Unit - a unique identifier for this specific variant.
     */
    @Column(nullable = false, unique = true, length = 100)
    private String sku;

    @Column(name = "price_cents", nullable = false)
    private int priceCents;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "EUR";

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Column(name = "stock_quantity", nullable = false)
    private int stockQuantity = 0;

    @Column(name = "reserved_quantity", nullable = false)
    private int reservedQuantity = 0;

    @Column(name = "is_available", nullable = false)
    private boolean available = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    /**
     * Updates the updatedAt timestamp before a database update.
     */
    @PreUpdate
    public void touch() {
        this.updatedAt = Instant.now();
    }

    // Getters and Setters...
    public Long getId() { return id; }
    public ProductEntity getProduct() { return product; }
    public void setProduct(ProductEntity product) { this.product = product; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public int getPriceCents() { return priceCents; }
    public void setPriceCents(int priceCents) { this.priceCents = priceCents; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }
    public int getReservedQuantity() { return reservedQuantity; }
    public void setReservedQuantity(int reservedQuantity) { this.reservedQuantity = reservedQuantity; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public UUID getUuid() { return uuid; }
}