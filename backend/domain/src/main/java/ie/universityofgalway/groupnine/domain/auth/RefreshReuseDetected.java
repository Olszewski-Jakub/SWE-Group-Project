package ie.universityofgalway.groupnine.domain.auth;

/**
 * Thrown when a rotated/old refresh token is presented again.
 */
public class RefreshReuseDetected extends RuntimeException {
    public RefreshReuseDetected(String message) {
        super(message);
    }
}

