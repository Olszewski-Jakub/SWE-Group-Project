package ie.universityofgalway.groupnine.buildlogic.convention.configuration

import ie.universityofgalway.groupnine.buildlogic.convention.extensions.implementation
import ie.universityofgalway.groupnine.buildlogic.convention.extensions.library
import ie.universityofgalway.groupnine.buildlogic.convention.extensions.libs
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureInfrastructure() {
    dependencies {
        implementation(libs.library("spring-context"))
        implementation(libs.library("jackson-databind"))
        implementation(project(":domain"))
        implementation(project(":service"))
        implementation(project(":util"))
    }
}
