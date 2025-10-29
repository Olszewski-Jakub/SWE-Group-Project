package ie.universityofgalway.groupnine.integration.db;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class FlywayRepairConfigTest {
    @Test
    void strategyRepairsThenMigrates() {
        FlywayRepairConfig cfg = new FlywayRepairConfig();
        var strat = cfg.repairThenMigrate();
        Flyway fly = mock(Flyway.class);
        strat.migrate(fly);
        verify(fly).repair();
        verify(fly).migrate();
    }
}

