package ie.universityofgalway.groupnine.domain.health;

/**
 * Domain-level health classification used by the health check use case.
 */
public enum HealthStatus {
    /**
     * System healthy and available.
     */
    UP,
    /**
     * System degraded or unavailable.
     */
    DOWN
}
