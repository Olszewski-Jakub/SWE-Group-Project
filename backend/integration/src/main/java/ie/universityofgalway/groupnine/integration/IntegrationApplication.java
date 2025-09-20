package ie.universityofgalway.groupnine.integration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Composition root.
 * - Scans all project packages for controllers/services/components/security.
 * - Scans JPA entities & Spring Data repos from infrastructure.
 */
@SpringBootApplication(scanBasePackages = "ie.universityofgalway.groupnine")
@EntityScan(basePackages = "ie.universityofgalway.groupnine.infrastructure")
//@EnableJpaRepositories(basePackages = "ie.universityofgalway.groupnine.infrastructure")
public class IntegrationApplication {
    public static void main(String[] args) {
        SpringApplication.run(IntegrationApplication.class, args);
    }
}
