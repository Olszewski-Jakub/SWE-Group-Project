package ie.universityofgalway.groupnine.testsupport.web;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.annotation.AliasFor;
import org.springframework.test.context.ContextConfiguration;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Meta-annotation for MVC slice tests in the delivery layer.
 * <p>
 * Wraps {@link WebMvcTest} and loads a minimal test application context
 * via {@link ContextConfiguration} pointing at {@link TestBoot}. Security
 * filters are disabled for convenience in controller unit tests.
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@WebMvcTest
@ContextConfiguration(classes = TestBoot.class)
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
public @interface DeliveryWebMvcTest {

    /**
     * Controllers to include in this MVC slice.
     * Alias to WebMvcTest.controllers.
     */
    @AliasFor(annotation = WebMvcTest.class, attribute = "controllers")
    Class<?>[] controllers() default {};
}
