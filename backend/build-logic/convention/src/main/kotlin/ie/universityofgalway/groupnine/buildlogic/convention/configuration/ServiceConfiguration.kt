package ie.universityofgalway.groupnine.buildlogic.convention.configuration

import ie.universityofgalway.groupnine.buildlogic.convention.extensions.implementation
import ie.universityofgalway.groupnine.buildlogic.convention.extensions.library
import ie.universityofgalway.groupnine.buildlogic.convention.extensions.libs
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureSerivce() {
    dependencies {
        implementation(project(":domain"))
        implementation(libs.library("spring-boot-starter"))
        implementation(project(":util"))
    }
}
