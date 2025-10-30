package ie.universityofgalway.groupnine.infrastructure.inventory.jpa;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "inventory")
public class InventoryAggregateEntity {
    @Id
    @Column(name = "variant_id", nullable = false, updatable = false)
    private UUID variantId;

    @Column(name = "reserved", nullable = false)
    private int reserved;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    @PrePersist
    public void touch() { this.updatedAt = Instant.now(); }

    public UUID getVariantId() { return variantId; }
    public void setVariantId(UUID variantId) { this.variantId = variantId; }
    public int getReserved() { return reserved; }
    public void setReserved(int reserved) { this.reserved = reserved; }
    public Instant getUpdatedAt() { return updatedAt; }
}

