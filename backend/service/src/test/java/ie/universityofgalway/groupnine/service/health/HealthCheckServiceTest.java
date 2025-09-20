package ie.universityofgalway.groupnine.service.health;

import ie.universityofgalway.groupnine.domain.health.HealthStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HealthCheckServiceTest {

    @Test
    @DisplayName("Returns UP when database is reachable")
    void returnsUpWhenDatabaseIsReachable() {
        DatabaseProbe databaseProbe = Mockito.mock(DatabaseProbe.class);
        Mockito.when(databaseProbe.pingDatabase()).thenReturn(true);
        HealthCheckService healthCheckService = new HealthCheckService(databaseProbe);

        assertEquals(HealthStatus.UP, healthCheckService.checkHealth());
    }

    @Test
    @DisplayName("Returns DOWN when database is unreachable")
    void returnsDownWhenDatabaseIsUnreachable() {
        DatabaseProbe databaseProbe = Mockito.mock(DatabaseProbe.class);
        Mockito.when(databaseProbe.pingDatabase()).thenReturn(false);
        HealthCheckService healthCheckService = new HealthCheckService(databaseProbe);

        assertEquals(HealthStatus.DOWN, healthCheckService.checkHealth());
    }
}
