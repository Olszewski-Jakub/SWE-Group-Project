package ie.universityofgalway.groupnine.domain.auth.exception;

/**
 * Thrown when a verification token is presented after its expiry time.
 */
public class ExpiredVerificationToken extends RuntimeException {
    public ExpiredVerificationToken(String message) {
        super(message);
    }
}
