package ie.universityofgalway.groupnine.domain.auth;

/**
 * Thrown when a refresh token is malformed or unknown.
 */
public class InvalidRefreshToken extends RuntimeException {
    public InvalidRefreshToken(String message) {
        super(message);
    }
}

