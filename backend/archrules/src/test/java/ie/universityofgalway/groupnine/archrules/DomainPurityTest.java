package ie.universityofgalway.groupnine.archrules;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Domain must be framework-agnostic.
 */
class DomainPurityTest {

    private static final String BASE = "ie.universityofgalway.groupnine";

    @Test
    void domain_must_not_depend_on_spring_or_jakarta_persistence_or_web() {
        var classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(BASE + ".domain");

        ArchRule rule = noClasses().that().resideInAPackage(BASE + ".domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework..",
                        "jakarta.persistence..",
                        "jakarta.servlet..",
                        "org.hibernate.."
                );
        rule.check(classes);
    }
}

