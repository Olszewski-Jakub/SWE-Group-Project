package ie.universityofgalway.groupnine.testsupport.web;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

/**
 * Minimal Boot configuration for MVC slice tests.
 * Scans only the delivery layer to keep the context small.
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@EntityScan(basePackages = "ie.universityofgalway.groupnine")
@ComponentScan(basePackages = "ie.universityofgalway.groupnine")
public class TestBoot { }
