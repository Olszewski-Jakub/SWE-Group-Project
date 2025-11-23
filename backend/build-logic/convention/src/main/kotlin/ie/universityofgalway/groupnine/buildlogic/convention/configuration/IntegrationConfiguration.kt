package ie.universityofgalway.groupnine.buildlogic.convention.configuration

import ie.universityofgalway.groupnine.buildlogic.convention.extensions.implementation
import ie.universityofgalway.groupnine.buildlogic.convention.extensions.library
import ie.universityofgalway.groupnine.buildlogic.convention.extensions.libs
import ie.universityofgalway.groupnine.buildlogic.convention.extensions.runtimeOnly
import ie.universityofgalway.groupnine.buildlogic.convention.extensions.testImplementation
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * Configure the :integration module.
 *
 * Usage from a precompiled convention plugin:
 *   fun Project.applyIntegrationConvention() = configureIntegration()
 */
internal fun Project.configureIntegration() {

    dependencies {
        implementation(libs.library("flywaydb-core"))
        implementation(libs.library("flywaydb-postgres"))
        implementation(libs.library("spring-boot-starter"))
        implementation(libs.library("spring-boot-starter-actuator"))
        implementation(libs.library("spring-boot-starter-data-jpa"))
        implementation(libs.library("spring-boot-starter-data-redis"))
        implementation(libs.library("spring-boot-starter-amqp"))
        implementation(libs.library("spring-boot.starter-data-redis"))
        implementation(libs.library("spring-boot-starter-security"))
        implementation(libs.library("spring-context"))
        implementation(libs.library("spring-security-crypto"))
        implementation(libs.library("stripe-java"))
        runtimeOnly(libs.library("postgresql"))

        testImplementation(libs.library("h2-database"))
        testImplementation(libs.library("spring-boot-starter-test"))
        testImplementation(platform(libs.library("junit-bom")))
    }
}
