package ie.universityofgalway.groupnine.infrastructure.product.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface VariantJpaRepository extends JpaRepository<VariantEntity, UUID> {
    Optional<VariantEntity> findByUuid(UUID uuid);
    boolean existsBySku(String sku);
}
