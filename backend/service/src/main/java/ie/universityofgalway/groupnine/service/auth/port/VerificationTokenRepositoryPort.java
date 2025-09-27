package ie.universityofgalway.groupnine.service.auth.port;

import ie.universityofgalway.groupnine.domain.auth.EmailVerificationToken;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Port for persisting and querying email verification tokens.
 */
public interface VerificationTokenRepositoryPort {
    /**
     * Save a new token aggregate.
     */
    EmailVerificationToken save(EmailVerificationToken token);

    /**
     * Look up a token aggregate by its stored hash.
     */
    Optional<EmailVerificationToken> findByHash(String tokenHash);

    /**
     * Mark a token as used at the given time.
     */
    void markUsed(UUID tokenId, Instant usedAt);
}
