package ie.universityofgalway.groupnine.service.health;

import ie.universityofgalway.groupnine.domain.health.HealthStatus;

/**
 * Application boundary for checking overall system health.
 */
public interface HealthCheckUseCase {
    /**
     * Evaluate the health of core dependencies and return a classification.
     */
    HealthStatus checkHealth();
}
