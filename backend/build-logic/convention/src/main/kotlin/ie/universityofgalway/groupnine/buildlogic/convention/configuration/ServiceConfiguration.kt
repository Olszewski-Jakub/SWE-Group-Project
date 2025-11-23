package ie.universityofgalway.groupnine.buildlogic.convention.configuration

import ie.universityofgalway.groupnine.buildlogic.convention.extensions.implementation
import ie.universityofgalway.groupnine.buildlogic.convention.extensions.library
import ie.universityofgalway.groupnine.buildlogic.convention.extensions.libs
import ie.universityofgalway.groupnine.buildlogic.convention.extensions.version
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureService() {
    dependencies {
        implementation(libs.library("jackson-databind"))
        implementation(libs.library("spring-context"))
        implementation(libs.library("spring-data-commons"))
        implementation(project(":domain"))
        implementation(project(":util"))
        implementation(libs.library("spring-boot-starter"))

    }
}
