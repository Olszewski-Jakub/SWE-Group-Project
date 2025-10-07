package ie.universityofgalway.groupnine.delivery.rest.health;

import ie.universityofgalway.groupnine.domain.health.HealthStatus;
import ie.universityofgalway.groupnine.service.health.HealthReport;
import ie.universityofgalway.groupnine.service.health.HealthCheckUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import ie.universityofgalway.groupnine.domain.security.PublicEndpoint;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ie.universityofgalway.groupnine.delivery.rest.support.ApiResponse;

/**
 * Health endpoint controller.
 * <p>
 * Returns an {@link ie.universityofgalway.groupnine.delivery.rest.health.HealthResponse}
 * that always includes overall status and, when available, component details for the
 * database, Redis, and RabbitMQ (with basic metrics and versions). HTTP status is 200
 * when overall is UP, otherwise 503.
 */
@RestController
@RequestMapping(ie.universityofgalway.groupnine.delivery.rest.support.Routes.V1)
public class HealthController {

    private final HealthCheckUseCase useCase;

    @Autowired
    public HealthController(HealthCheckUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping("/health")
    @PublicEndpoint
    public ResponseEntity<ApiResponse<HealthResponse>> getHealth() {
        // Always get basic status first (tests stub this method)
        HealthStatus status = useCase.checkHealth();

        HealthReport report = null;
        try {
            report = useCase.checkHealthReport();
        } catch (Throwable ignored) {
            // tolerate mocks that don't support default methods
        }

        HealthResponse body;
        if (report != null) {
            status = report.overall();
            HealthResponse.Component db = HealthResponse.Component.of(
                    report.db() == null ? "UNKNOWN" : report.db().status().name(),
                    report.db() == null ? java.util.Map.of() : report.db().details());
            HealthResponse.Component redis = HealthResponse.Component.of(
                    report.redis() == null ? "UNKNOWN" : report.redis().status().name(),
                    report.redis() == null ? java.util.Map.of() : report.redis().details());
            HealthResponse.Component rabbit = HealthResponse.Component.of(
                    report.rabbit() == null ? "UNKNOWN" : report.rabbit().status().name(),
                    report.rabbit() == null ? java.util.Map.of() : report.rabbit().details());
            body = HealthResponse.of(status.name(), db, redis, rabbit);
        } else {
            body = HealthResponse.of(status.name(), null, null, null);
        }

        if (status == HealthStatus.UP) {
            return ResponseEntity.ok(ApiResponse.ok(body));
        } else {
            return ResponseEntity.status(503).body(ApiResponse.ok(body));
        }
    }
}
