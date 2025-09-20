package ie.universityofgalway.groupnine.buildlogic.convention.configuration

import ie.universityofgalway.groupnine.buildlogic.convention.extensions.library
import ie.universityofgalway.groupnine.buildlogic.convention.extensions.libs
import ie.universityofgalway.groupnine.buildlogic.convention.extensions.testImplementation
import ie.universityofgalway.groupnine.buildlogic.convention.extensions.testRuntimeOnly
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType

internal fun Project.configureTesting() {
    dependencies {
        testImplementation(platform(libs.library("junit-bom")))
        testImplementation(libs.library("junit-jupiter"))
        testImplementation(libs.library("spring-boot-test"))
        testRuntimeOnly(libs.library("junit-platform-launcher"))
        testRuntimeOnly(libs.library("junit-platform-commons"))
        testRuntimeOnly(libs.library("junit-platform-engine"))
        testImplementation(libs.library("mockito-core"))
        testImplementation(libs.library("mockito-junit-jupiter"))
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        testLogging {
            events = setOf(
                TestLogEvent.FAILED, TestLogEvent.SKIPPED, TestLogEvent.PASSED
            )
            exceptionFormat = TestExceptionFormat.FULL
            showStandardStreams = false
        }
    }
}