package ie.universityofgalway.groupnine.service.auth.port;

import ie.universityofgalway.groupnine.domain.auth.PasswordResetToken;
import ie.universityofgalway.groupnine.domain.user.UserId;

import java.time.Instant;

/**
 * Port for persisting and invalidating password reset tokens.
 */
public interface PasswordResetTokenRepositoryPort {
    PasswordResetToken save(PasswordResetToken token);

    /**
     * Invalidate all active tokens for the user (e.g., by marking usedAt or expiring them).
     */
    void invalidateAllForUser(UserId userId, Instant when);

    /**
     * Find a password reset token by its stored hash.
     */
    java.util.Optional<PasswordResetToken> findByHash(String tokenHash);

    /**
     * Mark a token as used at the given time.
     */
    void markUsed(java.util.UUID tokenId, Instant usedAt);
}
