package ie.universityofgalway.groupnine.domain.auth;

/**
 * Thrown when a refresh token has expired.
 */
public class ExpiredRefreshToken extends RuntimeException {
    public ExpiredRefreshToken(String message) {
        super(message);
    }
}

