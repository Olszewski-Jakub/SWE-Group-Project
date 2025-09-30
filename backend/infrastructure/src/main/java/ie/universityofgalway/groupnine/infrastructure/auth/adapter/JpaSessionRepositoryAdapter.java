package ie.universityofgalway.groupnine.infrastructure.auth.adapter;

import ie.universityofgalway.groupnine.domain.session.Session;
import ie.universityofgalway.groupnine.domain.user.UserId;
import ie.universityofgalway.groupnine.infrastructure.auth.jpa.SessionEntity;
import ie.universityofgalway.groupnine.infrastructure.auth.jpa.SessionJpaRepository;
import ie.universityofgalway.groupnine.service.auth.port.SessionRepositoryPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA-backed session repository using optimized SQL for bulk operations and chain traversal.
 */
@Component
public class JpaSessionRepositoryAdapter implements SessionRepositoryPort {

    private final SessionJpaRepository repo;

    @Autowired
    public JpaSessionRepositoryAdapter(SessionJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public Session save(Session session) {
        SessionEntity e = toEntity(session);
        SessionEntity saved = repo.save(e);
        return toDomain(saved);
    }

    @Override
    public Optional<Session> findByRefreshTokenHash(String refreshTokenHash) {
        return repo.findByRefreshTokenHash(refreshTokenHash).map(this::toDomain);
    }

    @Override
    public Optional<Session> findById(UUID id) {
        return repo.findById(id).map(this::toDomain);
    }

    @Override
    public void revokeSession(UUID sessionId, Instant revokedAt, String reason, UUID replacedBy) {
        repo.markRevoked(sessionId, revokedAt, reason, replacedBy);
    }

    @Override
    public void revokeAllForUser(UserId userId, Instant revokedAt, String reason) {
        repo.revokeAllActiveForUser(userId.value(), revokedAt, reason);
    }

    @Override
    public void revokeChainFrom(UUID startSessionId, Instant revokedAt, String reason) {
        repo.revokeChainFrom(startSessionId, revokedAt, reason);
    }

    private Session toDomain(SessionEntity e) {
        InetAddress ip = null;
        if (e.getIpAddress() != null && !e.getIpAddress().isBlank()) {
            try { ip = InetAddress.getByName(e.getIpAddress()); } catch (UnknownHostException ignored) {}
        }
        return new Session(
                e.getId(),
                UserId.of(e.getUserId()),
                e.getRefreshTokenHash(),
                e.getUserAgent(),
                ip,
                e.getCreatedAt(),
                e.getExpiresAt(),
                e.getRevokedAt(),
                e.getReplacedBySessionId(),
                e.getReason()
        );
    }

    private SessionEntity toEntity(Session s) {
        SessionEntity e = new SessionEntity();
        e.setId(s.getId() == null ? UUID.randomUUID() : s.getId());
        e.setUserId(s.getUserId().value());
        e.setRefreshTokenHash(s.getRefreshTokenHash());
        e.setUserAgent(s.getUserAgent());
        e.setIpAddress(s.getIpAddress() == null ? null : s.getIpAddress().getHostAddress());
        e.setCreatedAt(s.getCreatedAt());
        e.setExpiresAt(s.getExpiresAt());
        e.setRevokedAt(s.getRevokedAt());
        e.setReplacedBySessionId(s.getReplacedBySessionId());
        e.setReason(s.getReason());
        return e;
    }
}
