package ie.universityofgalway.groupnine.domain.auth;

/**
 * Thrown when authentication attempts are rate-limited by the brute-force guard.
 */
public class TooManyAttempts extends RuntimeException {
    private final long retryAfterSeconds;

    public TooManyAttempts(String message, long retryAfterSeconds) {
        super(message);
        this.retryAfterSeconds = Math.max(0, retryAfterSeconds);
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}

