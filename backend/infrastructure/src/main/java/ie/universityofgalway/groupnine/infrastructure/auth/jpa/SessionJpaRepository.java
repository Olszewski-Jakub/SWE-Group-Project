package ie.universityofgalway.groupnine.infrastructure.auth.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface SessionJpaRepository extends JpaRepository<SessionEntity, UUID> {
    Optional<SessionEntity> findByRefreshTokenHash(String refreshTokenHash);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update SessionEntity s set s.revokedAt = :revokedAt, s.reason = :reason, s.replacedBySessionId = coalesce(:replacedBy, s.replacedBySessionId) where s.id = :id")
    int markRevoked(@Param("id") UUID id,
                    @Param("revokedAt") Instant revokedAt,
                    @Param("reason") String reason,
                    @Param("replacedBy") UUID replacedBy);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "update sessions set revoked_at = :revokedAt, reason = :reason where user_id = :userId and revoked_at is null", nativeQuery = true)
    int revokeAllActiveForUser(@Param("userId") UUID userId,
                               @Param("revokedAt") Instant revokedAt,
                               @Param("reason") String reason);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "with recursive chain as (\n" +
            "  select id, replaced_by_session_id from sessions where id = :start\n" +
            "  union all\n" +
            "  select s.id, s.replaced_by_session_id from sessions s\n" +
            "    join chain c on s.id = c.replaced_by_session_id\n" +
            ")\n" +
            "update sessions set revoked_at = :revokedAt, reason = :reason\n" +
            "where id in (select id from chain)", nativeQuery = true)
    int revokeChainFrom(@Param("start") UUID start,
                        @Param("revokedAt") Instant revokedAt,
                        @Param("reason") String reason);
}
