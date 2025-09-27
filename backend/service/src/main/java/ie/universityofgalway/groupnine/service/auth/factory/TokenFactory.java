package ie.universityofgalway.groupnine.service.auth.factory;

import ie.universityofgalway.groupnine.domain.auth.EmailVerificationToken;
import ie.universityofgalway.groupnine.domain.user.UserId;
import ie.universityofgalway.groupnine.service.auth.port.RandomTokenPort;

import java.time.Instant;


/**
 * Factory for creating authentication-related tokens in the application layer.
 * <p>
 * Currently supports email verification tokens by generating an opaque
 * base64url token and a corresponding SHA-256 hash to store server-side.
 */
public class TokenFactory {
    protected final RandomTokenPort randomTokenPort;

    public TokenFactory(RandomTokenPort randomTokenPort) {
        this.randomTokenPort = randomTokenPort;
    }

    /**
     * Create a new email verification token.
     *
     * @param userId    identifier of the user
     * @param createdAt token creation timestamp
     * @param expiresAt token expiry timestamp
     * @return pair containing the persisted token aggregate and the opaque value to send to the user
     */
    public VerificationTokenWithOpaque createEmailVerification(UserId userId, Instant createdAt, Instant expiresAt) {
        String opaque = randomTokenPort.generateOpaqueToken();
        String hash = randomTokenPort.sha256(opaque);
        EmailVerificationToken token = EmailVerificationToken.createNew(userId, hash, expiresAt, createdAt);
        return new VerificationTokenWithOpaque(token, opaque);
    }

    /**
     * Pair of persisted token aggregate and the opaque token string to be delivered to the user.
     */
    public record VerificationTokenWithOpaque(EmailVerificationToken token, String opaque) {
    }
}
