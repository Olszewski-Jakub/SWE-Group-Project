package ie.universityofgalway.groupnine.infrastructure.auth.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PasswordResetTokenJpaRepository extends JpaRepository<PasswordResetTokenEntity, UUID> {

    @Modifying
    @Query("update PasswordResetTokenEntity t set t.usedAt = :when where t.userId = :userId and t.usedAt is null")
    void invalidateAllForUser(@Param("userId") UUID userId, @Param("when") Instant when);

    Optional<PasswordResetTokenEntity> findByTokenHash(String tokenHash);
}
