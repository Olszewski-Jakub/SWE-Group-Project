package ie.universityofgalway.groupnine.integration.db;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayRepairConfig {

    /**
     * Optional strategy to repair Flyway schema history on startup before migrating.
     * Enable with: app.flyway.repair-on-start=true
     */
    @Bean
    @ConditionalOnProperty(prefix = "app.flyway", name = "repair-on-start", havingValue = "true")
    public FlywayMigrationStrategy repairThenMigrate() {
        return (Flyway flyway) -> {
            flyway.repair();
            flyway.migrate();
        };
    }
}
