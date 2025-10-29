package ie.universityofgalway.groupnine.domain.auth.exception;

/**
 * Thrown when a password reset token has expired.
 */
public class ExpiredPasswordResetToken extends RuntimeException {
    public ExpiredPasswordResetToken(String message) {
        super(message);
    }
}

