package ie.universityofgalway.groupnine.service.health;

import ie.universityofgalway.groupnine.domain.health.HealthStatus;

public interface HealthCheckUseCase {
    HealthStatus checkHealth();
}