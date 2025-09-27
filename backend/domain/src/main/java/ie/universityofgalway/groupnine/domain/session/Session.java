package ie.universityofgalway.groupnine.domain.session;

import ie.universityofgalway.groupnine.domain.user.UserId;

import java.net.InetAddress;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Session entity representing a refresh token session.
 * Stores only the refresh token hash; the opaque token value is never persisted.
 */
public class Session {
    private final UUID id;
    private final UserId userId;
    private final String refreshTokenHash;
    private final String userAgent;
    private final InetAddress ipAddress;
    private final Instant createdAt;
    private final Instant expiresAt;
    private final Instant revokedAt; // nullable
    private final UUID replacedBySessionId; // nullable
    private final String reason; // nullable

    public Session(UUID id, UserId userId, String refreshTokenHash, String userAgent, InetAddress ipAddress,
                   Instant createdAt, Instant expiresAt, Instant revokedAt, UUID replacedBySessionId, String reason) {
        this.id = id;
        this.userId = Objects.requireNonNull(userId);
        this.refreshTokenHash = Objects.requireNonNull(refreshTokenHash);
        this.userAgent = userAgent;
        this.ipAddress = ipAddress;
        this.createdAt = createdAt;
        this.expiresAt = Objects.requireNonNull(expiresAt);
        this.revokedAt = revokedAt;
        this.replacedBySessionId = replacedBySessionId;
        this.reason = reason;
    }

    public static Session createNew(UserId userId, String refreshTokenHash, String userAgent, InetAddress ipAddress, Instant now, Instant expiresAt) {
        return new Session(null, userId, refreshTokenHash, userAgent, ipAddress, now, expiresAt, null, null, null);
    }

    public Session revoke(Instant at, String reason, UUID replacedBy) {
        return new Session(id, userId, refreshTokenHash, userAgent, ipAddress, createdAt, expiresAt, at, replacedBy, reason);
    }

    public static Session createNewReplacing(UserId userId, String refreshTokenHash, String userAgent, InetAddress ipAddress,
                                             Instant now, Instant expiresAt, UUID replacedSessionId) {
        return new Session(null, userId, refreshTokenHash, userAgent, ipAddress, now, expiresAt, null, replacedSessionId, null);
    }

    public UUID getId() { return id; }
    public UserId getUserId() { return userId; }
    public String getRefreshTokenHash() { return refreshTokenHash; }
    public String getUserAgent() { return userAgent; }
    public InetAddress getIpAddress() { return ipAddress; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getRevokedAt() { return revokedAt; }
    public UUID getReplacedBySessionId() { return replacedBySessionId; }
    public String getReason() { return reason; }
}
