package ie.universityofgalway.groupnine.delivery.rest.health;

import java.util.Map;

/**
 * Health API response payload containing overall status and per-component sections.
 * Each component may include simple diagnostic details such as versions, runtime metrics,
 * and measured latency.
 */
public record HealthResponse(
        String status,
        Component db,
        Component redis,
        Component rabbit
) {
    public static HealthResponse of(String status, Component db, Component redis, Component rabbit) {
        return new HealthResponse(status, db, redis, rabbit);
    }

    /** Component health section with status and arbitrary diagnostic details. */
    public record Component(String status, Map<String, Object> details) {
        public static Component of(String status, Map<String, Object> details) {
            return new Component(status, details == null ? Map.of() : details);
        }
    }
}
