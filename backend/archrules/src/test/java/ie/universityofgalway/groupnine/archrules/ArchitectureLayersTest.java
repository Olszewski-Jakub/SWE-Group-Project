package ie.universityofgalway.groupnine.archrules;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.library.Architectures;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Layered dependencies for the flattened architecture.
 */
class ArchitectureLayersTest {

    private static final String BASE = "ie.universityofgalway.groupnine";

    @Test
    void service_should_not_depend_on_delivery_or_infrastructure() {
        var classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(BASE);

        ArchRule rule = noClasses().that().resideInAPackage(BASE + ".service..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        BASE + ".delivery..",
                        BASE + ".infrastructure.."
                );
        rule.check(classes);
    }

    @Test
    void infrastructure_should_not_depend_on_delivery() {
        var classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(BASE);

        ArchRule rule = noClasses().that().resideInAPackage(BASE + ".infrastructure..")
                .should().dependOnClassesThat().resideInAPackage(BASE + ".delivery..");
        rule.check(classes);
    }
}
