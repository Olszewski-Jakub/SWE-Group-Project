package ie.universityofgalway.groupnine.service.security.port;

import ie.universityofgalway.groupnine.domain.user.Email;

import java.net.InetAddress;

/**
 * Port for rate-limiting password reset requests per email and IP.
 */
public interface PasswordResetRateLimitPort {
    /**
     * Returns true if the request should be processed; false indicates internal suppression.
     */
    boolean allow(Email email, InetAddress ipAddress);
}

