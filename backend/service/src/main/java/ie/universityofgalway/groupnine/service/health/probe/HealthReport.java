package ie.universityofgalway.groupnine.service.health.probe;

import ie.universityofgalway.groupnine.domain.health.HealthStatus;

import java.util.Map;

/**
 * Aggregate health report for core infrastructure dependencies.
 * <p>
 * The report provides an overall status and per-component details for the
 * database, Redis, and RabbitMQ including optional metrics and version info.
 */
public record HealthReport(
        HealthStatus overall,
        ComponentReport db,
        ComponentReport redis,
        ComponentReport rabbit
) {
    public static HealthReport of(HealthStatus overall, ComponentReport db, ComponentReport redis, ComponentReport rabbit) {
        return new HealthReport(overall, db, redis, rabbit);
    }

    public static ComponentReport component(HealthStatus status, Map<String, Object> details) {
        return new ComponentReport(status, details == null ? Map.of() : details);
    }

    /**
     * Component-specific status and arbitrary details map (e.g., versions, metrics, latency).
     */
    public record ComponentReport(HealthStatus status, Map<String, Object> details) {}
}
