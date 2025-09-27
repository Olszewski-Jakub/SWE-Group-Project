package ie.universityofgalway.groupnine.service.auth.port;

import java.time.Instant;

/**
 * Abstraction over time retrieval to make business logic deterministic in tests.
 */
public interface ClockPort {
    /**
     * Returns the current instant in UTC.
     */
    Instant now();
}
