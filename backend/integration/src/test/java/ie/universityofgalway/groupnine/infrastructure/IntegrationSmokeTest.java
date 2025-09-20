package ie.universityofgalway.groupnine.infrastructure;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Minimal “does the context start” test.
 * Keeps CI green and catches wiring issues early.
 */
@SpringBootTest
@SpringBootApplication(scanBasePackages = "ie.universityofgalway.groupnine")
@EntityScan(basePackages = "ie.universityofgalway.groupnine.infrastructure")
class IntegrationSmokeTest {

    @Test
    void contextLoads() {
        // no-op
    }
}
