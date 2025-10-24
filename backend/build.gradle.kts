import org.gradle.api.tasks.testing.logging.TestLogEvent
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    // Dependency & plugin management
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.boot.dependencies) apply false
    alias(libs.plugins.versions) apply false
    jacoco
}

allprojects {
    group = "ie.universityofgalway.groupnine"
    version = project.findProperty("version")!!

    repositories {
        mavenCentral()
    }

    apply(plugin = "com.github.ben-manes.versions")
    tasks.withType<DependencyUpdatesTask>().configureEach {
        rejectVersionIf {
            val current = currentVersion
            val candidate = candidate.version
            isNonStable(candidate) && !isNonStable(current)
        }
        checkForGradleUpdate = true
        outputFormatter = "plain,html,json"
        outputDir = "${project.buildDir}/reports/dependencyUpdates"
        reportfileName = "report"
    }
}

subprojects {
    apply(plugin = "jacoco")

    plugins.withType<JavaPlugin> {
        extensions.configure<JavaPluginExtension> {
            toolchain.languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get()))
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging {
            events = setOf(
                TestLogEvent.FAILED,
                TestLogEvent.SKIPPED,
                TestLogEvent.PASSED
            )
        }
    }
}

// Aggregate JaCoCo report across all modules
tasks.register<JacocoReport>("jacocoRootReport") {
    val projectsWithTest = subprojects.filter { sub -> sub.tasks.findByName("test") != null }

    dependsOn(projectsWithTest.map { it.tasks.named("test") })

    executionData.setFrom(projectsWithTest.map { file("${it.buildDir}/jacoco/test.exec") })

    sourceDirectories.setFrom(projectsWithTest.flatMap {
        listOf(
            fileTree("${it.projectDir}/src/main/java"),
            fileTree("${it.projectDir}/src/main/kotlin")
        )
    })

    classDirectories.setFrom(projectsWithTest.flatMap {
        listOf(
            fileTree("${it.buildDir}/classes/java/main"),
            fileTree("${it.buildDir}/classes/kotlin/main")
        )
    })

    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

// Aggregate dependency updates across all modules
tasks.register("dependencyUpdatesAll") {
    group = "verification"
    description = "Run Gradle Versions Plugin across all modules."
    dependsOn(
        allprojects.mapNotNull { it.tasks.findByName("dependencyUpdates") }
    )
}

// One-stop verification task: run checks and dependency updates for all modules
tasks.register("verifyAll") {
    group = "verification"
    description = "Run tests, checks and dependency update reports for all modules."
    dependsOn(
        subprojects.mapNotNull { sub -> sub.tasks.findByName("check") }
    )
    dependsOn(tasks.named("dependencyUpdatesAll"))
}

// Helper used in dependency update filtering
fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    return !stableKeyword && !regex.matches(version)
}

tasks.register("verifyCoverageAll") {
    group = "verification"
    description = "Run all tests and verify JaCoCo coverage thresholds across all modules."

    dependsOn(
        subprojects.mapNotNull { sub ->
            sub.tasks.findByName("check")
        }
    )
}
