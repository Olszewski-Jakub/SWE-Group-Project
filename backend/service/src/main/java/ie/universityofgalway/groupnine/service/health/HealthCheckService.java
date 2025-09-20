package ie.universityofgalway.groupnine.service.health;

import ie.universityofgalway.groupnine.domain.health.HealthStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Application service that implements the HealthCheck use case.
 * Depends on a DatabaseProbe (output port) to determine database health.
 */
@Service
public class HealthCheckService implements HealthCheckUseCase {
    private final DatabaseProbe databaseProbe;

    @Autowired
    public HealthCheckService(DatabaseProbe databaseProbe) {
        this.databaseProbe = databaseProbe;
    }

    @Override
    public HealthStatus checkHealth() {
        boolean dbUp = databaseProbe.pingDatabase();
        // The logic: if the database (primary dependency) is reachable, we consider system UP.
        return dbUp ? HealthStatus.UP : HealthStatus.DOWN;
    }
}