package ie.universityofgalway.groupnine.service.auth.port;

import ie.universityofgalway.groupnine.domain.session.Session;
import ie.universityofgalway.groupnine.domain.user.UserId;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Port abstraction for persisting and revoking refresh-token sessions.
 * Implementations must ensure referential integrity and efficient bulk operations.
 */
public interface SessionRepositoryPort {
    /**
     * Persist a new or updated {@link Session} and return the saved instance.
     */
    Session save(Session session);

    /**
     * Look up a session by the stored refresh token hash.
     */
    Optional<Session> findByRefreshTokenHash(String refreshTokenHash);

    /**
     * Find a session by its primary key.
     */
    Optional<Session> findById(UUID id);

    /**
     * Mark a single session as revoked and optionally set a forward link to the replacing session.
     * Implementations must not clear an existing {@code replaced_by_session_id} when {@code replacedBy} is {@code null}.
     */
    void revokeSession(UUID sessionId, Instant revokedAt, String reason, UUID replacedBy);

    /**
     * Revoke all active sessions for a user by setting {@code revokedAt} and {@code reason}.
     * Implementations must not overwrite rotation metadata (e.g., {@code replaced_by_session_id} or existing reasons)
     * on sessions that are already revoked.
     */
    void revokeAllForUser(UserId userId, Instant revokedAt, String reason);

    /**
     * Revoke the session identified by {@code startSessionId} and all sessions reachable via the
     * forward chain using {@code replaced_by_session_id}, setting the provided reason uniformly.
     */
    void revokeChainFrom(UUID startSessionId, Instant revokedAt, String reason);
}
