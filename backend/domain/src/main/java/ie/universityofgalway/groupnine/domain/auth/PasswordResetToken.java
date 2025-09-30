package ie.universityofgalway.groupnine.domain.auth;

import ie.universityofgalway.groupnine.domain.user.UserId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate representing a single-use password reset token.
 * Stores only a secure hash of the opaque token value.
 */
public record PasswordResetToken(UUID id, UserId userId, String tokenHash, Instant expiresAt, Instant createdAt,
                                 Instant usedAt) {
    public PasswordResetToken {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(tokenHash, "tokenHash");
        Objects.requireNonNull(expiresAt, "expiresAt");
        Objects.requireNonNull(createdAt, "createdAt");
    }

    public static PasswordResetToken createNew(UserId userId, String tokenHash, Instant expiresAt, Instant createdAt) {
        return new PasswordResetToken(UUID.randomUUID(), userId, tokenHash, expiresAt, createdAt, null);
    }

    public PasswordResetToken markUsed(Instant when) {
        return new PasswordResetToken(id, userId, tokenHash, expiresAt, createdAt, when);
    }

    public boolean isUsed() { return usedAt != null; }

    public boolean isExpiredAt(Instant now) { return now.isAfter(expiresAt); }
}

