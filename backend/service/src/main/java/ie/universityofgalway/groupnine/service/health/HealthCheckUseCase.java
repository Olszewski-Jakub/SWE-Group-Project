package ie.universityofgalway.groupnine.service.health;

import ie.universityofgalway.groupnine.domain.health.HealthStatus;

/**
 * Application boundary for checking overall system health.
 */
public interface HealthCheckUseCase {
    /**
     * Evaluate the health of core dependencies and return a simple classification.
     * Consumers that require component-level details should use {@link #checkHealthReport()}.
     */
    HealthStatus checkHealth();

    /**
     * Extended health report including per-component status and details (e.g., versions, metrics).
     * <p>
     * Default implementation derives overall status from {@link #checkHealth()} and returns
     * empty component details; concrete services may override to include richer data.
     */
    default HealthReport checkHealthReport() {
        HealthStatus s = checkHealth();
        return HealthReport.of(
                s,
                HealthReport.component(HealthStatus.UP, java.util.Map.of()),
                HealthReport.component(HealthStatus.UP, java.util.Map.of()),
                HealthReport.component(HealthStatus.UP, java.util.Map.of())
        );
    }
}
