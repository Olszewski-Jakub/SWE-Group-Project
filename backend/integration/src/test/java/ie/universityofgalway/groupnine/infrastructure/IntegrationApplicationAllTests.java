package ie.universityofgalway.groupnine.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("IntegrationApplication - All Unit/Arch/Sanity Tests")
class IntegrationApplicationAllTests {

    @Test
    @DisplayName("main(String[]) is public static void")
    void hasPublicStaticVoidMain_acceptsStringArray() throws Exception {
        Method main = IntegrationApplication.class.getDeclaredMethod("main", String[].class);
        assertEquals(void.class, main.getReturnType(), "main should return void");
        assertTrue(java.lang.reflect.Modifier.isPublic(main.getModifiers()), "main should be public");
        assertTrue(java.lang.reflect.Modifier.isStatic(main.getModifiers()), "main should be static");
    }

    @Test
    @DisplayName("@SpringBootApplication scans project root package")
    void springBootApplication_scansProjectRoot() {
        SpringBootApplication sba = IntegrationApplication.class.getAnnotation(SpringBootApplication.class);
        assertNotNull(sba, "@SpringBootApplication should be present");

        String[] basePackages = sba.scanBasePackages();
        assertEquals(1, basePackages.length, "should have exactly one scanBasePackage");
        assertEquals("ie.universityofgalway.groupnine", basePackages[0]);
    }

    @Test
    @DisplayName("@EntityScan points to infrastructure package")
    void entityScan_pointsToInfrastructure() {
        EntityScan entityScan = IntegrationApplication.class.getAnnotation(EntityScan.class);
        assertNotNull(entityScan, "@EntityScan should be present");

        String[] basePackages = entityScan.basePackages();
        assertTrue(
                Arrays.asList(basePackages).contains("ie.universityofgalway.groupnine.infrastructure"),
                "EntityScan should include the infrastructure package"
        );
    }
}