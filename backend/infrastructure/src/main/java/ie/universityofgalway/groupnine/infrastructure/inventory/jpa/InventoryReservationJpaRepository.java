package ie.universityofgalway.groupnine.infrastructure.inventory.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InventoryReservationJpaRepository extends JpaRepository<InventoryReservationEntity, UUID> {
    Optional<InventoryReservationEntity> findByOrderId(UUID orderId);
}

