package ie.universityofgalway.groupnine.integration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Minimal “does the context start” test.
 * Reuses the main IntegrationApplication configuration.
 */
@SpringBootTest(classes = IntegrationApplication.class)
class IntegrationSmokeTest {

    @Test
    void contextLoads() {
        // no-op
    }
}
