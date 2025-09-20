package ie.universityofgalway.groupnine.archrules;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Service;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

/**
 * Ensure stereotypes live in expected modules/packages.
 */
class StereotypesLocationTest {

    private static final String BASE = "ie.universityofgalway.groupnine";

    private JavaClasses loadAll() {
        return new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(BASE);
    }

    @Test
    void controllers_must_live_in_delivery() {
        ArchRule rule = classes().that().areAnnotatedWith(RestController.class)
                .should().resideInAPackage(BASE + ".delivery..");
        rule.check(loadAll());
    }

    @Test
    void application_services_must_live_in_service() {
        ArchRule rule = classes().that().areAnnotatedWith(Service.class)
                .should().resideInAnyPackage(BASE + ".service..", BASE + ".security.."); // allow Security @Service if any
        rule.check(loadAll());
    }


    @Test
    void no_controllers_outside_delivery() {
        ArchRule rule = classes().that().areAnnotatedWith(RestController.class)
                .should().resideOutsideOfPackages(
                        BASE + ".domain..",
                        BASE + ".service..",
                        BASE + ".infrastructure..",
                        BASE + ".security..",
                        BASE + ".integration.."
                ).because("Controllers belong only to delivery-rest");
        rule.check(loadAll());
    }
}
