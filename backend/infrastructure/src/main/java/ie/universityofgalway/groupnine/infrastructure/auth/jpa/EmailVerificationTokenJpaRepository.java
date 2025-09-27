package ie.universityofgalway.groupnine.infrastructure.auth.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailVerificationTokenJpaRepository extends JpaRepository<EmailVerificationTokenEntity, UUID> {
    Optional<EmailVerificationTokenEntity> findByTokenHash(String tokenHash);
}
