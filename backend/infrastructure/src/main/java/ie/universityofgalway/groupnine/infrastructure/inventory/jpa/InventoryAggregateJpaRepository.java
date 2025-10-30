package ie.universityofgalway.groupnine.infrastructure.inventory.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface InventoryAggregateJpaRepository extends JpaRepository<InventoryAggregateEntity, UUID> {
    Optional<InventoryAggregateEntity> findByVariantId(UUID variantId);

    @Modifying
    @Query("update InventoryAggregateEntity i set i.reserved = i.reserved + :delta where i.variantId = :vid and i.reserved + :delta >= 0")
    int adjustReserved(@Param("vid") UUID variantId, @Param("delta") int delta);

    @Modifying
    @Query(value = "INSERT INTO inventory (variant_id, reserved) VALUES (:vid, 0) ON CONFLICT (variant_id) DO NOTHING", nativeQuery = true)
    int ensureRow(@Param("vid") UUID variantId);

    @Modifying
    @Query(value = "UPDATE inventory i SET reserved = reserved + :qty FROM product_variants v WHERE i.variant_id = :vid AND v.uuid = :vid AND (v.stock_quantity - i.reserved) >= :qty", nativeQuery = true)
    int reserveIfAvailable(@Param("vid") UUID variantId, @Param("qty") int qty);
}
