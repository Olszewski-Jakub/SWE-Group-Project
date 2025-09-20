package ie.universityofgalway.groupnine.archrules;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition;
import org.junit.jupiter.api.Test;

/**
 * No package cycles across features/layers.
 */
class CyclesFreeTest {

    private static final String BASE = "ie.universityofgalway.groupnine";

    @Test
    void packages_should_be_free_of_cycles() {
        var classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(BASE);

        SlicesRuleDefinition.slices()
                .matching(BASE + ".(*)..")
                .should().beFreeOfCycles()
                .check(classes);
    }
}
