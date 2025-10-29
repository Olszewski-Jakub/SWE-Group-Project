package ie.universityofgalway.groupnine.domain.auth.exception;

/**
 * Thrown when a verification token has already been consumed.
 */
public class TokenAlreadyUsed extends RuntimeException {
    public TokenAlreadyUsed(String message) {
        super(message);
    }
}
