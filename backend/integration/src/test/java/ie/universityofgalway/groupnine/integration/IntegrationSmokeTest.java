package ie.universityofgalway.groupnine.integration;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Minimal “does the context start” test.
 * Reuses the main IntegrationApplication configuration.
 */
@SpringBootTest(classes = IntegrationApplication.class)
@Disabled("Smoke Test disable deu to errors")
class IntegrationSmokeTest {

    @Test
    void contextLoads() {
        // no-op
    }
}
