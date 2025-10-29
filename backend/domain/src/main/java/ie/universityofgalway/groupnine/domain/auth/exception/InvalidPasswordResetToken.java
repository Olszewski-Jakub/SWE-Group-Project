package ie.universityofgalway.groupnine.domain.auth.exception;

/**
 * Thrown when a password reset token is missing or invalid.
 */
public class InvalidPasswordResetToken extends RuntimeException {
    public InvalidPasswordResetToken(String message) {
        super(message);
    }
}

