package ie.universityofgalway.groupnine.infrastructure.messaging.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxMessageJpaRepository extends JpaRepository<OutboxMessageEntity, UUID> {
    @Query("select m from OutboxMessageEntity m where m.publishedAt is null order by m.createdAt asc")
    List<OutboxMessageEntity> findUnpublished();
}

