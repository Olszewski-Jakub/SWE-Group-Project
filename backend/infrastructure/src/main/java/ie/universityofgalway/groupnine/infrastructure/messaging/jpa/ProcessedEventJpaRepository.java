package ie.universityofgalway.groupnine.infrastructure.messaging.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedEventJpaRepository extends JpaRepository<ProcessedEventEntity, ProcessedEventId> {
    boolean existsBySourceAndKey(String source, String key);
}

