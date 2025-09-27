package ie.universityofgalway.groupnine.delivery.rest.health;

import ie.universityofgalway.groupnine.domain.health.HealthStatus;
import ie.universityofgalway.groupnine.service.health.HealthCheckUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing a DB-backed health check.
 * Clean Architecture: depends only on application use case.
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    private final HealthCheckUseCase useCase;

    @Autowired
    public HealthController(HealthCheckUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping("/health")
    public ResponseEntity<HealthResponse> getHealth() {
        HealthStatus status = useCase.checkHealth();
        HealthResponse body = new HealthResponse(status.name());

        if (status == HealthStatus.UP) {
            return ResponseEntity.ok(body);
        } else {
            return ResponseEntity.status(503).body(body);
        }
    }
}
