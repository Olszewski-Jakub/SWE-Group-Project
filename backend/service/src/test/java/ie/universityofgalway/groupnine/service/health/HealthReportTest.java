package ie.universityofgalway.groupnine.service.health;

import ie.universityofgalway.groupnine.domain.health.HealthStatus;
import ie.universityofgalway.groupnine.service.health.probe.HealthReport;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HealthReportTest {
    @Test
    void buildersCreateImmutableReport() {
        HealthReport.ComponentReport db = HealthReport.component(HealthStatus.UP, Map.of("v","1"));
        HealthReport r = HealthReport.of(HealthStatus.DOWN, db, HealthReport.component(HealthStatus.UP, Map.of()), HealthReport.component(HealthStatus.UP, Map.of()));
        assertEquals(HealthStatus.DOWN, r.overall());
        assertEquals(HealthStatus.UP, r.db().status());
        assertEquals("1", r.db().details().get("v"));
    }
}

