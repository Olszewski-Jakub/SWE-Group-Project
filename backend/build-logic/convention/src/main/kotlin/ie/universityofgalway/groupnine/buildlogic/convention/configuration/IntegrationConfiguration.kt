// build-logic/src/main/kotlin/ie/universityofgalway/groupnine/buildlogic/convention/configuration/IntegrationConfiguration.kt
package ie.universityofgalway.groupnine.buildlogic.convention.configuration

import ie.universityofgalway.groupnine.buildlogic.convention.extensions.implementation
import ie.universityofgalway.groupnine.buildlogic.convention.extensions.library
import ie.universityofgalway.groupnine.buildlogic.convention.extensions.libs
import ie.universityofgalway.groupnine.buildlogic.convention.extensions.runtimeOnly
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
        implementation(project(":domain"))
        implementation(project(":service"))
        implementation(project(":infrastructure"))
        implementation(project(":security"))
        implementation(project(":delivery:rest"))
        implementation(project(":util"))
        implementation(libs.library("spring-boot-starter-actuator"))
        implementation(libs.library("spring-boot-starter"))
        implementation(libs.library("spring-boot-starter-data-jpa"))
        implementation(libs.library("spring-boot-starter-data-redis"))
        implementation(libs.library("spring-context"))
        runtimeOnly(libs.library("postgresql"))
    }
}
