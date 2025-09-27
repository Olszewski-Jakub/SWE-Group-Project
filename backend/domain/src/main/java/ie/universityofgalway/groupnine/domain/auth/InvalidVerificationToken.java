package ie.universityofgalway.groupnine.domain.auth;

/**
 * Thrown when a provided verification token is missing or does not match any stored token.
 */
public class InvalidVerificationToken extends RuntimeException {
    public InvalidVerificationToken(String message) {
        super(message);
    }
}
