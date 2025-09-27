import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    // Dependency & plugin management
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.boot.dependencies) apply false
    jacoco
}

allprojects {
    group = "com.example"
    version = project.findProperty("version")!!

    repositories {
        mavenCentral()
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
    dependsOn(subprojects.map { it.tasks.named("test") })

    executionData.setFrom(subprojects.map { file("${it.layout.buildDirectory}/jacoco/test.exec") })
    sourceDirectories.setFrom(subprojects.map { it.fileTree("src/main/java") })
    classDirectories.setFrom(subprojects.map { it.fileTree("build/classes/java/main") })

    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.register("verifyCoverageAll") {
    group = "verification"
    description = "Run all tests and verify JaCoCo coverage thresholds across all modules."

    // depends on every subproject's `check` (which itself runs tests + jacoco verification)
    dependsOn(
        subprojects.mapNotNull { sub ->
            sub.tasks.findByName("check")
        }
    )
}
