package ie.universityofgalway.groupnine.testsupport.web;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.annotation.AliasFor;
import org.springframework.test.context.ContextConfiguration;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@WebMvcTest
@ContextConfiguration(classes = TestBoot.class)
public @interface DeliveryWebMvcTest {

    /**
     * Controllers to include in this MVC slice.
     * Alias to WebMvcTest.controllers.
     */
    @AliasFor(annotation = WebMvcTest.class, attribute = "controllers")
    Class<?>[] controllers() default {};
}
