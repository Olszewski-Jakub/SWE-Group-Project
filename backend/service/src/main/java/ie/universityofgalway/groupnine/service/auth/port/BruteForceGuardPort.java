package ie.universityofgalway.groupnine.service.auth.port;

import ie.universityofgalway.groupnine.domain.user.Email;

import java.net.InetAddress;

/**
 * Strategy for rate-limiting and tracking authentication attempts.
 * Implementations may use in-memory counters, Redis, or external services.
 */
public interface BruteForceGuardPort {
    /**
     * Decide whether an authentication attempt should be permitted.
     */
    boolean allowAttempt(Email email, InetAddress ipAddress);

    /**
     * Record a successful authentication attempt.
     */
    void recordSuccess(Email email, InetAddress ipAddress);

    /**
     * Record a failed authentication attempt.
     */
    void recordFailure(Email email, InetAddress ipAddress);

    /**
     * If the caller is currently rate-limited, return the number of seconds to wait before retrying.
     * Implementations may return {@code null} when no backoff is in effect or when not supported.
     */
    default Long getRetryAfterSeconds(Email email, InetAddress ipAddress) { return null; }

    /**
     * Combined check returning the decision and an optional suggested retry-after seconds.
     * Default implementation derives from {@link #allowAttempt} and {@link #getRetryAfterSeconds}.
     */
    default Verdict check(Email email, InetAddress ipAddress) {
        boolean allowed = allowAttempt(email, ipAddress);
        Long retry = allowed ? null : getRetryAfterSeconds(email, ipAddress);
        return new Verdict(allowed, retry);
    }

    /** Immutable result of a brute-force decision. */
    record Verdict(boolean allowed, Long retryAfterSeconds) {}
}
