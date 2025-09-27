package ie.universityofgalway.groupnine.testsupport.web;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Minimal Spring Boot application used to anchor test contexts.
 * <p>
 * Restricts component scanning to the delivery REST layer so MVC slice tests
 * load quickly while still discovering controllers and controller advice.
 */
@SpringBootApplication(scanBasePackages = "ie.universityofgalway.groupnine.delivery.rest")
public class TestBoot {
}
