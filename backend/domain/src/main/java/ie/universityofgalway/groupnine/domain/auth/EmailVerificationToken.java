package ie.universityofgalway.groupnine.domain.auth;

import ie.universityofgalway.groupnine.domain.user.UserId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate representing a one-time email verification token.
 * <p>
 * Stores a secure hash of an opaque token value along with lifecycle timestamps
 * and the owning user identifier. The opaque value is never persisted.
 *
 * @param tokenHash base64url-encoded SHA-256 of the opaque token
 * @param usedAt    timestamp when consumed; {@code null} if unused
 */
public record EmailVerificationToken(UUID id, UserId userId, String tokenHash, Instant expiresAt, Instant createdAt,
                                     Instant usedAt) {
    public EmailVerificationToken(UUID id, UserId userId, String tokenHash, Instant expiresAt, Instant createdAt, Instant usedAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.userId = Objects.requireNonNull(userId, "userId");
        this.tokenHash = Objects.requireNonNull(tokenHash, "tokenHash");
        this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.usedAt = usedAt;
    }

    public static EmailVerificationToken createNew(UserId userId, String tokenHash, Instant expiresAt, Instant createdAt) {
        return new EmailVerificationToken(UUID.randomUUID(), userId, tokenHash, expiresAt, createdAt, null);
    }

    public EmailVerificationToken markUsed(Instant when) {
        return new EmailVerificationToken(id, userId, tokenHash, expiresAt, createdAt, when);
    }

    public boolean isUsed() {
        return usedAt != null;
    }

    public boolean isExpiredAt(Instant now) {
        return now.isAfter(expiresAt);
    }
}
