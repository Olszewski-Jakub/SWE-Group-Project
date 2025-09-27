package ie.universityofgalway.groupnine.infrastructure.audit.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AuditEventJpaRepository extends JpaRepository<AuditEventEntity, UUID> {
}

